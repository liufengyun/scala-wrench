package org.scalaverify
package test


object DSL {
  def file(f: String)(implicit flags: TestFlags): TestCase = TestCase.file(f)
  def directory(f: String)(implicit flags: TestFlags): TestCase = TestCase.directory(f)
  def filesInDir(f: String)(implicit flags: TestFlags): List[TestCase] = TestCase.filesInDir(f)

  def (inputs: List[TestCase]) compile: List[CompileOutput] = inputs.map(_.compile)
  def (outputs: List[CompileOutput]) shouldSucceed(implicit ctx: TestContext): Unit = outputs.map(_.shouldSucceed)
  def (outputs: List[CompileOutput]) shouldFail(implicit ctx: TestContext): Unit = outputs.map(_.shouldFail)
}