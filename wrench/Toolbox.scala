package org.xmid
package wrench

import java.io.{File => JFile, PrintWriter, StringWriter, BufferedWriter, BufferedReader, InputStreamReader }
import java.nio.file.Paths
import java.util.HashMap
import java.time.Duration

import scala.io.Source
import scala.collection.JavaConversions._

import dotty.tools._
import dotc.Driver
import dotc.interfaces.Diagnostic.ERROR
import dotc.reporting.diagnostic.MessageContainer
import dotc.reporting._


object Toolbox {
  def compile(files: List[JFile], flags: TestFlags): (Reporter, String) = {
    val sw: StringWriter = new StringWriter()
    val bw: BufferedWriter = new BufferedWriter(sw)
    val ps = new PrintWriter(bw)
    val reporter = new ConsoleReporter(writer = ps)
    val driver = new Driver
    driver.process(flags.all ++ files.map(_.getPath), reporter = reporter)
    (reporter, sw.toString)
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

  def checkErrors(files: Seq[JFile], reporterErrors: List[MessageContainer])(fail: String => Unit) = {
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

  /** Run the main class with the given classpath for the given duration (milliseconds) */
  def run(cp: List[String], mainClass: String, duration: Int): (Int, String) = {
    val javaBin = Paths.get(sys.props("java.home"), "bin", "java").toString
    val process = new ProcessBuilder(javaBin, "-Dfile.encoding=UTF-8", "-Xmx1g", "-cp", cp.mkString(JFile.pathSeparator), mainClass)
      .redirectErrorStream(true)
      .redirectInput(ProcessBuilder.Redirect.PIPE)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .start()

    var childStdout: BufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream, "UTF-8"))

    val sb = new StringBuilder

    var start = System.currentTimeMillis
    var line: String = childStdout.readLine()
    while (process.isAlive && (System.currentTimeMillis - start) < duration && line != null) {
      sb.append(line).append(System.lineSeparator)
      line = childStdout.readLine()
    }

    (process.exitValue, sb.toString)
  }
}
