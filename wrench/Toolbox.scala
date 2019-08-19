package org.xmid
package wrench

import java.io.{File => JFile, PrintWriter, FileWriter, BufferedWriter }
import java.util.HashMap
import scala.io.Source
import scala.collection.JavaConversions._

import dotty.tools._
import dotc.Driver
import dotc.interfaces.Diagnostic.ERROR
import dotc.reporting.diagnostic.MessageContainer
import dotc.reporting._

object Toolbox {
  def compile(files: List[JFile], flags: TestFlags, log: JFile): Reporter = {
    val fw: FileWriter = new FileWriter(log, /* append = */ true)
    val bw: BufferedWriter = new BufferedWriter(fw)
    val ps = new PrintWriter(bw)
    val reporter = new ConsoleReporter(writer = ps)
    val driver = new Driver
    driver.process(flags.all ++ files.map(_.getPath), reporter = reporter)
    reporter
  }

  // We collect these in a map `"file:row" -> numberOfErrors`
  private def getErrorMapAndExpectedCount(files: Seq[JFile]): HashMap[String, Integer] = {
    val errorMap = new HashMap[String, Integer]()
    files.filter(_.getName.endsWith(".scala")).foreach { file =>
      Source.fromFile(file, "UTF-8").getLines().zipWithIndex.foreach { case (line, lineNbr) =>
        val errors = line.sliding("// error".length).count(_.mkString == "// error")
        if (errors > 0)
          errorMap.put(s"${file.getPath}:${lineNbr}", errors)
      }
    }

    errorMap
  }

  def checkErros(files: Seq[JFile], reporterErrors: List[MessageContainer])(fail: String => Unit) = {
    import scala.language.implicitConversions

    val errorMap: HashMap[String, Integer] = getErrorMapAndExpectedCount(files)

    reporterErrors.foreach { error =>
      if (error.pos.exists) {
        def toRelative(path: String): String = path.split("/").dropWhile(_ != "tests").mkString("/")
        val fileName = toRelative(error.pos.source.file.toString)
        val key = s"$fileName:${error.pos.line}"

        val errors = errorMap.get(key)

        if (errors ne null) {
          if (errors > 0) errorMap.put(key, errors - 1)
          else fail("More errors reported at " + key)
        }
        else fail(s"Error reported at $key, but no annotation found")
      }
      else fail("unexpected error without pos: " + error.message)
    }

    errorMap.foreach { case (k, v) =>
      if (v != 0) fail("Fewer errors reported at " + k + ", expect " + v + " more")
    }
  }
}