package org.scalaverify
package test

import java.io.{File => JFile}

final case class TestCase(name: String, flags: TestFlags, targetDir: JFile, files: List[JFile], log: JFile) {
  def compile: CompileOutput = {
    val out = targetDir.getAbsolutePath()
    val flags2 = flags.and("-d", out).withClassPath(out)
    targetDir.mkdirs()
    val reporter = Toolbox.compile(files, flags2, log)
    CompileOutput(this, reporter)
  }
}

object TestCase {
  private def outDir(file: JFile): JFile =
    new JFile(
      Defaults.rootOutputDir + JFile.separator +
        file.getName.replaceFirst("[.][^.]+$", "") + JFile.separator
    )

  private def logFile(file: JFile): JFile = new JFile(file.getAbsolutePath() + ".out")

  /** A single file from the string path `f` using the supplied flags */
  def file(f: String)(implicit flags: TestFlags): TestCase = {
    val sourceFile = new JFile(f)
    assert(sourceFile.exists(), s"the file ${sourceFile.getAbsolutePath()} does not exist")
    TestCase(f, flags, outDir(sourceFile), sourceFile :: Nil, logFile((sourceFile)))
  }

  /** A directory `f` using the supplied `flags`. This method does
   *  deep compilation, that is - it compiles all files and subdirectories
   *  contained within the directory `f`.
   */
  def directory(dir: String, recursive: Boolean = true)(implicit flags: TestFlags): TestCase = {
    val sourceDir = new JFile(dir)

    assert(sourceDir.exists(), s"the directory ${sourceDir.getAbsolutePath()} does not exist")

    def flatten(f: JFile): List[JFile] =
      if (f.isDirectory) {
        val files = f.listFiles.toList
        if (recursive) files.flatMap(flatten) else files
      }
      else List(f)

    val sortedFiles = flatten(sourceDir).sorted

    TestCase(dir, flags, outDir(sourceDir), sortedFiles, logFile(sourceDir))
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
      if (f.getName().endsWith(".scala") || f.getName().endsWith(".java")) file(f.getPath) :: inputs
      else if (f.isDirectory) directory(f.getPath) :: inputs
      else inputs
    }
  }
}
