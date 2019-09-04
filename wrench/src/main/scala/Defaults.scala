package org.xmid
package wrench

import java.io.File

import org.xmid.wrench._

object Defaults {
  val noCheckOptions = Map(
    "-pagewidth" -> "120",
    "-color:never" -> ""
  )

  val checkOptions = Map(
    "-Yno-deep-subtypes" -> "",
    "-Yno-double-bindings" -> "",
    "-Yforce-sbt-phases" -> "",
    "-Xverify-signatures" -> "",
  )

  val basicClasspath = List(
    Properties.scalaLibrary,
    Properties.dottyLibrary
  )

  val compilerClasspath = List(
    Properties.scalaLibrary,
    Properties.scalaAsm,
    Properties.jlineTerminal,
    Properties.jlineReader,
    Properties.compilerInterface,
    Properties.dottyInterfaces,
    Properties.dottyLibrary,
    Properties.dottyCompiler
  )

  val yCheckOptions = Map("-Ycheck:all" -> "")

  val commonOptions = checkOptions ++ noCheckOptions ++ yCheckOptions
  def defaultOptions(implicit ctx: TestContext) = TestFlags(basicClasspath, commonOptions)
}
