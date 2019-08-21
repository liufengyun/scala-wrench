package org.xmid
package wrench

import java.io.{File => JFile}
import dotty.tools.dotc.reporting.diagnostic.MessageContainer

import wrench.Util._

final case class CompileOutput(test: TestCase, output: String, errors: List[MessageContainer]) {
  private[wrench] def checkCompile(implicit ctx: TestContext): Boolean = {
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

  private[wrench] def checkSucceeded(implicit ctx: TestContext): Boolean = {
    if (!errors.isEmpty) dumpLog
    errors.isEmpty
  }
}