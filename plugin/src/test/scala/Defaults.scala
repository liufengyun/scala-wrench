package org.scalaverify
package test

import java.io.File

object Defaults {
  val rootOutputDir = "out" + File.separator

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
  val defaultOptions = TestFlags(basicClasspath, commonOptions)
  val withCompilerOptions =
    defaultOptions.withClassPath(compilerClasspath).withRunClassPath(compilerClasspath)
  val allowDeepSubtypes = defaultOptions without "-Yno-deep-subtypes"
  val allowDoubleBindings = defaultOptions without "-Yno-double-bindings"
  val picklingOptions = defaultOptions and (
    "-Xprint-types" -> "",
    "-Ytest-pickler" -> "",
    "-Yprint-pos" -> "",
    "-Yprint-pos-syms" -> ""
  )
  val picklingWithCompilerOptions =
    picklingOptions.withClassPath(compilerClasspath).withRunClassPath(compilerClasspath)
  val scala2Mode = defaultOptions and "-language:Scala2"
  val explicitUTF8 = defaultOptions and ("-encoding" -> "UTF8")
  val explicitUTF16 = defaultOptions and ("-encoding" -> "UTF16")
}
