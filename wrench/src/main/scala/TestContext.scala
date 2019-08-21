package org.xmid
package wrench

import scala.collection.mutable
import scala.language.implicitConversions

/** `TestContext` can be used by unit tests by utilizing `@AfterClass` to
 *  call `echoSummary`
 *
 */
trait TestContext {
  /** Report a test as passing */
  def passed(test: TestCase): Unit

  /** Add the name of the failed test */
  def failed(test: TestCase): Unit

  /** Print message */
  def info(msg: String): Unit

  /** Print error message */
  def error(msg: String): Unit

  /** Echo the summary report to the appropriate locations */
  def echoSummary(): Unit

  /** Clean up after test */
  def cleanup: Unit

  /** Timeout in running a single test */
  def runTimeout: Int
}

final class DefaultContext(val runTimeout: Int = 2000) extends TestContext {
  import scala.collection.JavaConverters._

  private val failedTests = new scala.collection.mutable.ListBuffer[TestCase]
  private val passedTests = new scala.collection.mutable.ListBuffer[TestCase]

  private[this] var passed = 0

  def failed(test: TestCase): Unit =
    failedTests += test

  def passed(test: TestCase): Unit =
  passedTests += test

  def info(msg: String): Unit = println(msg)

  def error(msg: String): Unit = println("[error] " + msg)

  /** Both echoes the summary to stdout and prints to file */
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
}
