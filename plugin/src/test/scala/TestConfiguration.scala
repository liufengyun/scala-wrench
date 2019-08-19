package org.scalaverify
package test

import java.io.File

object TestConfiguration {

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

  val basicClasspath = mkClasspath(List(
    Properties.scalaLibrary,
    Properties.dottyLibrary
  ))

  val compilerClasspath = mkClasspath(List(
    Properties.scalaLibrary,
    Properties.scalaAsm,
    Properties.jlineTerminal,
    Properties.jlineReader,
    Properties.compilerInterface,
    Properties.dottyInterfaces,
    Properties.dottyLibrary,
    Properties.dottyCompiler
  ))

  def mkClasspath(classpaths: List[String]): String =
    classpaths.map({ p =>
      val file = new java.io.File(p)
      assert(file.exists, s"File $p couldn't be found.")
      file.getAbsolutePath
    }).mkString(File.pathSeparator)

  val yCheckOptions = Map("-Ycheck:all" -> "")

  val commonOptions = checkOptions ++ noCheckOptions ++ yCheckOptions
  val defaultOptions = TestFlags(basicClasspath, commonOptions)
  val withCompilerOptions =
    defaultOptions.withClasspath(compilerClasspath).withRunClasspath(compilerClasspath)
  val allowDeepSubtypes = defaultOptions without "-Yno-deep-subtypes"
  val allowDoubleBindings = defaultOptions without "-Yno-double-bindings"
  val picklingOptions = defaultOptions and (
    "-Xprint-types" -> "",
    "-Ytest-pickler" -> "",
    "-Yprint-pos" -> "",
    "-Yprint-pos-syms" -> ""
  )
  val picklingWithCompilerOptions =
    picklingOptions.withClasspath(compilerClasspath).withRunClasspath(compilerClasspath)
  val scala2Mode = defaultOptions and "-language:Scala2"
  val explicitUTF8 = defaultOptions and ("-encoding" -> "UTF8")
  val explicitUTF16 = defaultOptions and ("-encoding" -> "UTF16")
}
