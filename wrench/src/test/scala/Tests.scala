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
  def posTests = filesInDir("tests/pos").shouldCompile

  @Test
  def negTests = filesInDir("tests/neg").shouldNotCompile

  @Test
  def runTests = filesInDir("tests/run").shouldRun
}

object Tests {
  implicit val testCtx: TestContext = new DefaultContext
  @AfterClass def cleanup(): Unit = {
    testCtx.echoSummary()
    testCtx.cleanup
  }
}