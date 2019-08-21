package org.xmid
package wrench

import java.nio.file._

/** Runtime properties from defines or environmnent */
object Properties {
  private val classPath = sys.props("java.class.path").split(":")

  private def findLib(name: String): String =
    classPath.find(_.namePart.contains(name)) match {
      case Some(path) => path
      case _ =>
        throw new ClassNotFoundException(s"Cannot find library $name on class path. Do you forget to add a dependency on the Dotty compiler?")
    }

  /** dotty-interfaces jar */
  def dottyInterfaces: String = findLib("dotty-interfaces")

  /** dotty-library jar */
  def dottyLibrary: String = findLib("dotty-library")

  /** dotty-compiler jar */
  def dottyCompiler: String = findLib("dotty-compiler")

  /** compiler-interface jar */
  def compilerInterface: String = findLib("compiler-interface")

  /** scala-library jar */
  def scalaLibrary: String = findLib("scala-library")

  /** scala-asm jar */
  def scalaAsm: String = findLib("scala-asm")

  /** jline-terminal jar */
  def jlineTerminal: String = findLib("jline-terminal")

  /** jline-reader jar */
  def jlineReader: String = findLib("jline-reader")
}
