package org.scalaverify
package test

import org.junit.{ Test, BeforeClass, AfterClass }
import org.junit.Assert._

import DSL._

class Tests {
  import Tests._

  implicit val flags: TestFlags = Defaults.defaultOptions.and("-fatal-warnings")

  @Test
  def initPosTests = filesInDir("tests/init/pos").compile.shouldSucceed

  @Test
  def initNegTests = filesInDir("tests/init/neg").compile.shouldFail
}

object Tests {
  implicit val testCtx: TestContext = new DefaultContext
  @AfterClass def cleanup(): Unit = testCtx.echoSummary()
}