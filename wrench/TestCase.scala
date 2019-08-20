package org.xmid
package wrench

import java.io.{File => JFile}

trait TestCase {
  def name: String
  def log: JFile
  def sources: List[JFile]

  def compile: CompileOutput
}

case class FileTestCase(name: String, flags: TestFlags, targetDir: JFile, file: JFile, log: JFile) extends TestCase {
  def sources: List[JFile] = file :: Nil

  def compile: CompileOutput = {
    val out = targetDir.getAbsolutePath()
    val flags2 = flags.and("-d", out).withClassPath(out)
    targetDir.mkdirs()

    val reporter = Toolbox.compile(file :: Nil, flags2, log)
    targetDir.deleteRecursive()
    CompileOutput(this, reporter.allErrors)
  }
}

case class DirectoryTestCase(name: String, sourceDir: JFile, flags: TestFlags, targetDir: JFile, sources: List[JFile], log: JFile, groupable: Boolean) extends TestCase {
  def compile: CompileOutput = {
    val out = targetDir.getAbsolutePath()
    val flags2 = flags.and("-d", out).withClassPath(out)
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

      val errors = batches.flatMap { batch =>
        val reporter = Toolbox.compile(batch, flags2, log)
        reporter.allErrors
      }
      targetDir.deleteRecursive()
      CompileOutput(this, errors)
    }
    else {
      val reporter = Toolbox.compile(sources, flags2, log)
      targetDir.deleteRecursive()
      CompileOutput(this, reporter.allErrors)
    }
  }
}

object TestCase {
  private def outDir(file: JFile)(implicit ctx: TestContext): JFile =
    new JFile(
      ctx.rootOutDirectory + JFile.separator +
        file.getName.withoutExtension + JFile.separator
    )

  private def logFile(file: JFile): JFile = new JFile(file.getAbsolutePath() + ".out").ensureFresh()

  /** A single file from the string path `f` using the supplied flags */
  def file(f: String)(implicit flags: TestFlags, ctx: TestContext): TestCase = {
    val sourceFile = new JFile(f)
    assert(sourceFile.exists(), s"the file ${sourceFile.getAbsolutePath()} does not exist")
    FileTestCase(f, flags, outDir(sourceFile), sourceFile, logFile((sourceFile)))
  }

  /** A directory `f` using the supplied `flags`. This method does
   *  deep compilation, that is - it compiles all files and subdirectories
   *  contained within the directory `f`.
   */
  def directory(dir: String, recursive: Boolean = true)(implicit flags: TestFlags, ctx: TestContext): TestCase = {
    val sourceDir = new JFile(dir)

    assert(sourceDir.exists(), s"the directory ${sourceDir.getAbsolutePath()} does not exist")

    def flatten(f: JFile): List[JFile] =
      if (f.isDirectory) {
        val files = f.listFiles.toList
        if (recursive) files.flatMap(flatten) else files
      }
      else List(f)

    val sortedFiles = flatten(sourceDir).sorted

    DirectoryTestCase(dir, sourceDir, flags, outDir(sourceDir), sortedFiles, logFile(sourceDir), groupable = !recursive)
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
  def filesInDir(dir: String)(implicit flags: TestFlags, ctx: TestContext): List[TestCase] = {
    val f = new JFile(dir)
    assert(f.exists(), "the directory " + f.getAbsolutePath + " does not exist")
    f.listFiles.foldLeft(List.empty[TestCase]) { case (inputs, f) =>
      if (f.getName().endsWith(".scala") || f.getName().endsWith(".java")) file(f.getPath) :: inputs
      else if (f.isDirectory) directory(f.getPath, recursive = false) :: inputs
      else inputs
    }
  }
}
