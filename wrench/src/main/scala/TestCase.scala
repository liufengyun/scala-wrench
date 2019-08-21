package org.xmid
package wrench

import java.io.{File => JFile}

import dotty.tools.dotc.reporting.diagnostic.MessageContainer

import wrench.Util._
trait TestCase {
  def name: String
  def sources: List[JFile]
  def runCheckFile: Option[JFile]
  def targetDir: JFile
  def compileLogPath: String
  def runLogPath: String

  private[wrench] def compile(implicit ctx: TestContext): CompileOutput
  private[wrench] def run(implicit ctx: TestContext): RunOutput

  def cleanup: Unit = {
    targetDir.deleteRecursive()
  }
}

case class FileTestCase(name: String, flags: TestFlags, targetDir: JFile, file: JFile) extends TestCase {
  private val out = targetDir.getAbsolutePath()

  val sources: List[JFile] = file :: Nil

  lazy val compileLogPath = file.getAbsolutePath() + ".out"
  lazy val runLogPath = file.ofExtension(".check").getAbsolutePath + ".out"

  lazy val runCheckFile: Option[JFile] = {
    val checkFile = file.ofExtension(".check")
    if (checkFile.exists()) Some(checkFile)
    else None
  }

  def compile(implicit ctx: TestContext): CompileOutput = {
    ctx.info("compiling " + this.name)
    val flags2 = flags.and("-d" -> out).withClassPath(out)
    targetDir.mkdirs()

    val (reporter, output) = Toolbox.compile(file :: Nil, flags2)
    CompileOutput(this, output, reporter.allErrors)
  }

  def run(implicit ctx: TestContext): RunOutput = {
    ctx.info("running " + this.name)
    val (exitCode, output) = Toolbox.run(out :: flags.classPath, "Test", 2000)
    RunOutput(this, exitCode, output)
  }
}

case class DirectoryTestCase(name: String, sourceDir: JFile, flags: TestFlags, targetDir: JFile, sources: List[JFile], groupable: Boolean) extends TestCase {
  private val out = targetDir.getAbsolutePath()

  lazy val compileLogPath = new JFile(sourceDir, sourceDir.getName() + ".check.out").getAbsolutePath()
  lazy val runLogPath = new JFile(sourceDir, sourceDir.getName() + ".out").getAbsolutePath()

  lazy val runCheckFile: Option[JFile] = {
    val checkFile = new JFile(sourceDir, sourceDir.getName() + ".check")
    if (checkFile.exists()) Some(checkFile)
    else None
  }

  def compile(implicit ctx: TestContext): CompileOutput = {
    ctx.info("compiling " + this.name)
    val flags2 = flags.and("-d" -> out).withClassPath(out)
    targetDir.mkdirs()

    def endsWithNum(f: JFile): Boolean = {
      val name = f.getName().withoutExtension
      val index = name.lastIndexOf('_')
      index > 0 && {
        val suffix = name.substring(index + 1, name.length)
        suffix.forall(Character.isDigit)
      }
    }

    if (groupable && sources.forall(endsWithNum)) {
      val batches = sources.groupBy[Int] { file =>
        val name = file.getName().withoutExtension
        name.substring(name.lastIndexOf('_') + 1, name.length).toInt
      }.toList.sortBy(_._1).map(_._2)

      val sb: StringBuilder = new StringBuilder
      val errors = batches.flatMap { batch =>
        val (reporter, output) = Toolbox.compile(batch, flags2)
        sb ++= output
        reporter.allErrors
      }
      CompileOutput(this, sb.mkString, errors)
    }
    else {
      val (reporter, output) = Toolbox.compile(sources, flags2)
      CompileOutput(this, output, reporter.allErrors)
    }
  }

  def run(implicit ctx: TestContext): RunOutput = {
    ctx.info("running " + this.name)
    val (exitCode, output) = Toolbox.run(out :: flags.classPath, "Test", 2000)
    RunOutput(this, exitCode, output)
  }
}

object TestCase {
  /** A single file from the string path `f` using the supplied flags */
  def file(f: String)(implicit flags: TestFlags): TestCase = {
    val sourceFile = new JFile(f)
    assert(sourceFile.exists(), s"the file ${sourceFile.getAbsolutePath()} does not exist")
    val outDir = new JFile(sourceFile.getParentFile(), sourceFile.getName().withoutExtension)
    FileTestCase(f, flags, outDir, sourceFile)
  }

  /** A directory `f` using the supplied `flags`. This method does
   *  deep compilation, that is - it compiles all files and subdirectories
   *  contained within the directory `f`.
   */
  def directory(dir: String, recursive: Boolean = false)(implicit flags: TestFlags): DirectoryTestCase = {
    val sourceDir = new JFile(dir)

    assert(sourceDir.exists(), s"the directory ${sourceDir.getAbsolutePath()} does not exist")

    def flatten(f: JFile): List[JFile] =
      if (f.isDirectory) {
        val files = f.listFiles.toList
        if (recursive) files.flatMap(flatten)
        else files.filter(_.isScalaOrJava)
      }
      else if (f.isScalaOrJava)
        List(f)
      else Nil

    val sortedFiles = flatten(sourceDir).sorted

    val outDir = sourceDir.child("out")
    DirectoryTestCase(dir, sourceDir, flags, outDir, sortedFiles, groupable = !recursive)
  }

  /** This function creates a list of TestCase for the files and folders
   *   contained within directory `f`.
   *
   *  For this function to work as expected, we use the same convention for
   *  directory layout as the old partest. That is:
   *
   *  - Single files can have an associated check-file with the same name (but
   *    with file extension `.check`)
   *  - Directories can have an associated check-file, where the check file has
   *    the same name as the directory (with the file extension `.check`)
   */
  def filesInDir(dir: String)(implicit flags: TestFlags): List[TestCase] = {
    val f = new JFile(dir)
    assert(f.exists(), "the directory " + f.getAbsolutePath + " does not exist")
    f.listFiles.foldLeft(List.empty[TestCase]) { case (inputs, f) =>
      if (f.isScalaOrJava) file(f.getPath) :: inputs
      else if (f.isDirectory) directory(f.getPath, recursive = false) :: inputs
      else inputs
    }
  }
}
