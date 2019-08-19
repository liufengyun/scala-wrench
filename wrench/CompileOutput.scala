package org.xmid
package wrench

import java.io.{File => JFile}
import dotty.tools.dotc.reporting.diagnostic.MessageContainer


final case class CompileOutput(input: TestCase, errors: List[MessageContainer]) {
  def shouldFail(implicit ctx: TestContext): Unit = {
    ctx.echo("tesing " + input.name)
    var failed: Boolean = false
    Toolbox.checkErros(input.files, errors) { msg =>
      ctx.error(msg)
      failed = true
    }
    if (failed) ctx.reportFailed(input)
    else ctx.reportPassed(input)
  }

  def shouldSucceed(implicit ctx: TestContext): Unit = {
    ctx.echo("tesing " + input.name)
    if (errors.nonEmpty) ctx.reportFailed(input)
    else ctx.reportPassed(input)
  }

  // TODO: add `shouldRun`
}