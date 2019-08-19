package org.xmid
package wrench

import java.io.{File => JFile}

inline def (s: String) withoutExtension: String =
  s.replaceFirst("[.][^.]+$", "")

def (f: JFile) ensureFresh(): JFile = {
  if (f.exists()) f.delete()
  f
}


def (f: JFile) deleteRecursive(): Unit =
  if (f.isDirectory) {
    val files = f.listFiles.toList
    files.foreach(_.deleteRecursive())
    f.delete
  }
  else f.delete()
