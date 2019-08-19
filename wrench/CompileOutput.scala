package org.scalawrench

import java.io.{File => JFile}
import dotty.tools.dotc.reporting.Reporter


final case class CompileOutput(input: TestCase, reporter: Reporter) {
  def shouldFail(implicit ctx: TestContext): Unit = {
    ctx.echo("tesing " + input.name)
    var failed: Boolean = false
    Toolbox.checkErros(input.files, this.reporter.allErrors) { msg =>
      ctx.error(msg)
      failed = true
    }
    if (failed) ctx.reportFailed(input)
    else ctx.reportPassed(input)
  }

  def shouldSucceed(implicit ctx: TestContext): Unit = {
    ctx.echo("tesing " + input.name)
    if (reporter.hasErrors) ctx.reportFailed(input)
    else ctx.reportPassed(input)
  }

  // TODO: add `shouldRun`
}