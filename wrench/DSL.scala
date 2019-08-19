package org.scalawrench

def file(f: String) given TestFlags, TestContext: TestCase = TestCase.file(f)
def directory(f: String) given TestFlags, TestContext: TestCase = TestCase.directory(f)
def filesInDir(f: String) given TestFlags, TestContext: List[TestCase] = TestCase.filesInDir(f)

def (inputs: List[TestCase]) compile: List[CompileOutput] = inputs.map(_.compile)
def (outputs: List[CompileOutput]) shouldSucceed(implicit ctx: TestContext): Unit = outputs.map(_.shouldSucceed)
def (outputs: List[CompileOutput]) shouldFail(implicit ctx: TestContext): Unit = outputs.map(_.shouldFail)