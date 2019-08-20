package org.xmid
package wrench

def file(f: String) given TestFlags, TestContext: TestCase = TestCase.file(f)
def directory(f: String) given TestFlags, TestContext: TestCase = TestCase.directory(f)
def filesInDir(f: String) given TestFlags, TestContext: List[TestCase] = TestCase.filesInDir(f)

def (test: TestCase) shouldCompile(implicit ctx: TestContext): Unit = {
  if (test.compile.checkSucceeded) ctx.reportPassed(test)
  else ctx.reportFailed(test)
  test.cleanup
}

def (test: TestCase) shouldNotCompile(implicit ctx: TestContext): Unit = {
  if (test.compile.checkFailed) ctx.reportPassed(test)
  else ctx.reportFailed(test)
  test.cleanup
}

def (test: TestCase) shouldRun(implicit ctx: TestContext): Unit = {
  if (test.compile.checkSucceeded && test.run.checkSucceeded) ctx.reportPassed(test)
  else ctx.reportFailed(test)
  test.cleanup
}

def (tests: List[TestCase]) shouldCompile(implicit ctx: TestContext): Unit = tests.map(_.shouldCompile)
def (tests: List[TestCase]) shouldNotCompile(implicit ctx: TestContext): Unit = tests.map(_.shouldNotCompile)
def (tests: List[TestCase]) shouldRun(implicit ctx: TestContext): Unit = tests.map(_.shouldRun)

