package org.scalaverify
package test

import org.junit.{ Test, BeforeClass, AfterClass }
import org.junit.Assert._

import DSL._

class Tests {
  import Tests._

  implicit val flags: TestFlags = Defaults.defaultOptions.and("-Xfatal-warnings")

  @Test
  def posTests = filesInDir("tests/pos").compile.shouldSucceed

  @Test
  def negTests = filesInDir("tests/neg").compile.shouldFail

  @Test
  def initPosTests = ()

  @Test
  def initNegTests = ()
}

object Tests {
  implicit val testCtx: TestContext = new DefaultContext
  @AfterClass def cleanup(): Unit = testCtx.echoSummary()
}