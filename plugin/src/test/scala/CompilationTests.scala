package org.scalaverify
package test

import org.junit.{ Test, BeforeClass, AfterClass }
import org.junit.Assert._

import DSL._

class CompilationTests {
    implicit val flags: TestFlags = Defaults.defaultOptions.and("-fatal-warnings")
    implicit val testCtx: TestContext = new DefaultContext

    @Test
    def initPosTests = filesInDir("tests/init/pos").compile.shouldSucceed

    @Test
    def initNegTests = filesInDir("tests/init/neg").compile.shouldFail

    @AfterClass def cleanup(): Unit = testCtx.echoSummary()

}