package org.scalaverify
package test

import java.nio.file._

/** Runtime properties from defines or environmnent */
object Properties {
  /** Filter out tests not matching the regex supplied by "filter" define
   */
  val testsFilter: Option[String] = sys.props.get("filter")

  /** plugin jar */
  val plugin: Option[String] = sys.props.get("plugin")

  /** dotty-interfaces jar */
  def dottyInterfaces: String = sys.props("dottyInterfaces")

  /** dotty-library jar */
  def dottyLibrary: String = sys.props("dottyLibrary")

  /** dotty-compiler jar */
  def dottyCompiler: String = sys.props("dottyCompiler")

  /** compiler-interface jar */
  def compilerInterface: String = sys.props("compilerInterface")

  /** scala-library jar */
  def scalaLibrary: String = sys.props("scalaLibrary")

  /** scala-asm jar */
  def scalaAsm: String = sys.props("scalaAsm")

  /** jline-terminal jar */
  def jlineTerminal: String = sys.props("jlineTerminal")

  /** jline-reader jar */
  def jlineReader: String = sys.props("jlineReader")
}
