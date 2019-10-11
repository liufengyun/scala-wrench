package org.xmid
package wrench

import scala.collection.mutable
import scala.concurrent._

/** Context for performing test actions */
trait ActionContext {
  /** Print message */
  def info(msg: String): Unit

  /** Print error message */
  def error(msg: String): Unit
}

/** Context for performing test actions and reporting test result */
trait TestContext extends ActionContext {
  /** Timeout in running a single test */
  def runTimeout: Int

  /** Timeout in compiling a single test */
  def compileTimeout: Int

  /** Report a test as passing */
  def passed(test: TestCase): Unit

  /** Add the name of the failed test */
  def failed(test: TestCase): Unit

  /** Print summary report */
  def echoSummary(): Unit

  /** Clean up after test */
  def cleanup: Unit

  /** A transaction where evens happen on the given ActionContext were commited atomically */
  def transaction(op: (given ActionContext) => Unit): Unit

  /** Parallelize a list of operations */
  def (tests: List[TestCase]) parallelize(op: TestCase => Unit): Unit
}

/** Used in parallel setting to commit all logs from one single test atomically
 *
 *  @note This context must be used sequentially
 */
private[wrench] class StoredActionContext(ctx: TestContext) extends ActionContext {
  private val infoBuf: mutable.ListBuffer[String] = new mutable.ListBuffer()
  private val errorBuf: mutable.ListBuffer[String] = new mutable.ListBuffer()

  /** Print message */
  def info(msg: String): Unit = infoBuf += msg

  /** Print error message */
  def error(msg: String): Unit = errorBuf += msg

  /** Timeout in running a single test */
  def runTimeout: Int = ctx.runTimeout

  /** Commit logs atomically */
  def commit: Unit = ctx.synchronized {
    infoBuf.foreach(ctx.info(_))
    errorBuf.foreach(ctx.error(_))
    infoBuf.clear()
    errorBuf.clear()
  }
}

class DefaultContext extends TestContext {
  val runTimeout: Int = 2000
  val compileTimeout: Int = 1000

  protected  val failedTests = new scala.collection.mutable.ListBuffer[TestCase]
  protected val passedTests = new scala.collection.mutable.ListBuffer[TestCase]

  private[this] var passed = 0

  def failed(test: TestCase): Unit =
    failedTests += test

  def passed(test: TestCase): Unit =
    passedTests += test

  def info(msg: String): Unit = println(msg)

  def error(msg: String): Unit = println("[error] " + msg)

  def echoSummary(): Unit = {
    val failed = failedTests.size
    val passed = passedTests.size

    println(
      s"""|
          |================================================================================
          |Test Report
          |================================================================================
          |
          |$passed suites passed, $failed failed, ${passed + failed} total
          |""".stripMargin
    )

    failedTests.foreach { input =>
      val name = input.name
      println(s"    $name")
    }
  }

  def cleanup: Unit = ()

  def transaction(op: (given ActionContext) => Unit): Unit = op(given this)

  def (tests: List[TestCase]) parallelize(op: TestCase => Unit): Unit =
    tests.foreach(op(_))
}

class ParallelContext extends DefaultContext {
  override def failed(test: TestCase): Unit = failedTests.synchronized {
    failedTests += test
  }

  override def passed(test: TestCase): Unit = passedTests.synchronized {
    passedTests += test
  }

  override def (tests: List[TestCase]) parallelize(op: TestCase => Unit): Unit = {
    import ExecutionContext.Implicits.global
    import duration.Duration

    val tasks = tests.map { test => Future(op(test)) }
    val future = Future.sequence(tasks)
    Await.result(future, Duration.Inf)
  }

  override def transaction(op: (given ActionContext) => Unit): Unit = {
    val actionCtx: StoredActionContext = new StoredActionContext(this)
    op(given actionCtx)
    actionCtx.commit
  }
}