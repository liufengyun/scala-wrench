package org.scalaverify
package test

import java.io.{File => JFile}

final case class TestFlags(
  defaultClassPath: String,
  runClassPath: String, // class path that is used when running `run` tests (not compiling)
  options: Map[String, String]) { // only valid compiler options can be stored directly in `options`

  def and(flag: String): TestFlags =
    copy(options = options + (flag -> ""))

  def and(flag: String, value: String): TestFlags =
    copy(options = options + (flag -> value))

  def and(pairs: Tuple2[String, String]*): TestFlags =
    copy(options = options ++ pairs)

  def without(flags: String*): TestFlags =
    copy(options = options -- flags)

  def withClasspath(classPath: String): TestFlags =
    copy(defaultClassPath = s"$defaultClassPath${JFile.pathSeparator}$classPath")

  def withRunClasspath(classPath: String): TestFlags =
    copy(runClassPath = s"$runClassPath${JFile.pathSeparator}$classPath")

  def all: Array[String] = Array("-classpath", defaultClassPath) ++ options.flatMap { (k, v) =>
    if (v.length == 0) Array(k) else Array(k, v)
  }

  /** Subset of the flags that should be passed to javac. */
  def javacFlags: Array[String] = {
    val flags = all
    val cp = flags.dropWhile(_ != "-classpath").take(2)
    val output = flags.dropWhile(_ != "-d").take(2)
    cp ++ output
  }
}

object TestFlags {
  def apply(classPath: String, options: Map[String, String]): TestFlags =
    TestFlags(classPath, classPath, options)

  val defaultOutputDir = "out" + JFile.separator
}
