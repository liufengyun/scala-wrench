package org.xmid
package wrench

import scala.language.implicitConversions
import scala.collection.immutable.ArraySeq

import java.io.{File => JFile}

object Util {
  def (s: String) withoutExtension: String =
    s.replaceFirst("[.][^.]+$", "")

  def (path: String) namePart: String =
    new JFile(path).getName

  def (s: String) toLines: Seq[String] = s.split("\\r?\\n").toSeq

  def (f: JFile) ensureFresh(): JFile = {
    if (f.exists()) f.delete()
    f
  }

  def (f: JFile) ofExtension(ext: String): JFile =
    new JFile(f.getAbsolutePath().withoutExtension + ext)

  def (f: JFile) deleteRecursive(): Unit =
    if (f.isDirectory) {
      val files = f.listFiles.toList
      files.foreach(_.deleteRecursive())
      f.delete
    }
    else f.delete()


  def (f: JFile) isScalaOrJava: Boolean =
    f.getName().endsWith(".scala") || f.getName().endsWith(".java")

  def (f: JFile) isScala: Boolean =
    f.getName().endsWith(".scala")

  def (f: JFile) child(name: String): JFile =
    new JFile(f, name)
}