package org.xmid
package wrench
package test

import org.junit.{ Test, BeforeClass, AfterClass }
import org.junit.Assert._

import org.xmid.wrench._

class Tests {
  import Tests._

  implicit val flags: TestFlags = Defaults.defaultOptions.and("-Xfatal-warnings")

  @Test
  def posTests = testsIn("tests/pos").shouldCompile

  @Test
  def negTests = testsIn("tests/neg").checkCompile

  @Test
  def runTests = testsIn("tests/run").shouldRun

  @Test
  def pluginDivZeroTests = withPlugin("tests/plugins/divideZero") {
    testsIn("tests/plugins/divideZeroTests").checkCompile
  }
}

object Tests {
  implicit val testCtx: TestContext = new DefaultContext
  @AfterClass def cleanup(): Unit = {
    testCtx.echoSummary()
    testCtx.cleanup
  }
}