package org.scalaverify
package test


object DSL {
  def file(f: String)(implicit flags: TestFlags): CompileInput = CompileInput.file(f)
  def directory(f: String)(implicit flags: TestFlags): CompileInput = CompileInput.directory(f)
  def filesInDir(f: String)(implicit flags: TestFlags): List[CompileInput] = CompileInput.filesInDir(f)

  def (inputs: List[CompileInput]) compile: List[CompileOutput] = inputs.map(_.compile)
  def (outputs: List[CompileOutput]) shouldSucceed(implicit ctx: TestContext): Unit = outputs.map(_.shouldSucceed)
  def (outputs: List[CompileOutput]) shouldFail(implicit ctx: TestContext): Unit = outputs.map(_.shouldFail)
}