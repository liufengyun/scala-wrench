package org.xmid
package wrench

import java.io.{File => JFile}

import wrench.Util._

final case class RunOutput(test: TestCase, exitCode: Int, output: String) {
  private[wrench] def checkSucceeded(implicit ctx: TestContext): Boolean = {
    if (exitCode != 0) {
      ctx.error(test.name + " exited with " + exitCode)
      ctx.error("output: " + output)
      false
    }
    else if (test.runCheckFile.nonEmpty) {
      val checkFilePath = test.runCheckFile.get.getAbsolutePath()
      FileDiff.check(test.name, output.toLines, checkFilePath) match {
        case Some(msg) =>
          ctx.error(msg)
          FileDiff.dump(test.runLogPath, output.toLines)
          ctx.error(FileDiff.diffMessage(checkFilePath, test.runLogPath))
          false
        case _ =>
          true
      }
    }
    else true
  }
}
