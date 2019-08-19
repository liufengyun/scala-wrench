package org.scalaverify
package test

import scala.collection.mutable
import scala.language.implicitConversions

/** `SummaryReporting` can be used by unit tests by utilizing `@AfterClass` to
 *  call `echoSummary`
 *
 *  This is used in vulpix by passing the companion object's `SummaryReporting`
 *  to each test, the `@AfterClass def` then calls the `SummaryReport`'s
 *  `echoSummary` method in order to dump the summary to both stdout and a log
 *  file
 */
trait SummaryReporting {
  /** Report a test as passing */
  def reportPassed(test: String): Unit

  /** Add the name of the failed test */
  def reportFailed(test: String): Unit

  /** Print message */
  def echo(msg: String): Unit

  /** Print error message */
  def error(msg: String): Unit

  /** Echo the summary report to the appropriate locations */
  def echoSummary(): Unit
}

/** A summary report that logs to both stdout and the `TestReporter.logWriter`
 *  which outputs to a log file in `./testlogs/`
 */
final class SummaryReport extends SummaryReporting {
  import scala.collection.JavaConverters._

  private val failedTests = new scala.collection.mutable.ListBuffer[String]

  private[this] var passed = 0

  def reportFailed(test: String): Unit =
    failedTests += test

  def reportPassed(test: String): Unit =
    passed += 1

  def echo(msg: String): Unit = println(msg)

  def error(msg: String): Unit = println("[error] " + msg)

  /** Both echoes the summary to stdout and prints to file */
  def echoSummary(): Unit = {
    val failed = failedTests.size

    println(
      s"""|
          |================================================================================
          |Test Report
          |================================================================================
          |
          |$passed suites passed, $failed failed, ${passed + failed} total
          |""".stripMargin
    )

    failedTests.map(x => s"    $x\n").foreach(println)
  }
}
