package org.scalaverify
package test

import java.io.{File => JFile}

final case class CompileOutput(input: CompileInput, reporter: TestReporter) {
  def shouldFail(implicit summary: SummaryReporting): Unit = {
    summary.echo("tesing " + input.name)
    var failed: Boolean = false
    Toolbox.checkErros(input.files, this.reporter.errors) { msg =>
      summary.error(msg)
      failed = true
    }
    if (failed) summary.reportFailed(input.name)
    else summary.reportPassed(input.name)
  }

  def shouldSucceed(implicit summary: SummaryReporting): Unit = {
    summary.echo("tesing " + input.name)
    if (reporter.hasErrors) summary.reportFailed(input.name)
    else summary.reportPassed(input.name)
  }

  // TODO: add `shouldRun`
}