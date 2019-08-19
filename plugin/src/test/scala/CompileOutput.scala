package org.scalaverify
package test

import java.io.{File => JFile}

final case class CompileOutput(input: CompileInput, reporter: TestReporter) {
  def shouldFail(implicit ctx: TestContext): Unit = {
    ctx.echo("tesing " + input.name)
    var failed: Boolean = false
    Toolbox.checkErros(input.files, this.reporter.errors) { msg =>
      ctx.error(msg)
      failed = true
    }
    if (failed) ctx.reportFailed(input.name)
    else ctx.reportPassed(input.name)
  }

  def shouldSucceed(implicit ctx: TestContext): Unit = {
    ctx.echo("tesing " + input.name)
    if (reporter.hasErrors) ctx.reportFailed(input.name)
    else ctx.reportPassed(input.name)
  }

  // TODO: add `shouldRun`
}