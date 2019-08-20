package org.xmid
package wrench

import java.io.{File => JFile}
import dotty.tools.dotc.reporting.diagnostic.MessageContainer


final case class CompileOutput(test: TestCase, output: String, errors: List[MessageContainer]) {
  def checkFailed(implicit ctx: TestContext): Boolean = {
    var checkSuccess: Boolean = true
    Toolbox.checkErrors(test.sources, errors) { msg =>
      ctx.error(msg)
      checkSuccess = false
    }
    if (!checkSuccess) dumpLog
    checkSuccess
  }

  private def dumpLog(implicit ctx: TestContext) = {
    FileDiff.dump(test.compileLogPath, output.toLines)
    ctx.error("Compile failed. Check log file for more detail: " + test.compileLogPath)
  }

  def checkSucceeded(implicit ctx: TestContext): Boolean = {
    if (!errors.isEmpty) dumpLog
    errors.isEmpty
  }
}