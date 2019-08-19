package org.scalaverify
package test

import java.io.{File => JFile}

final case class TestInput(name: String, flags: TestFlags, targetDir: JFile, files: List[JFile]) {
  def compile: TestOutput = {
    val out = targetDir.getAbsolutePath()
    val flags2 = flags.and("-d", out).withClasspath(out)
    targetDir.mkdirs()
    val testReporter = Toolbox.compile(files, flags2)
    TestOutput(this, testReporter)
  }
}

object TestInput {
  private def outDir(file: JFile): JFile =
    new JFile(
      TestFlags.defaultOutputDir + JFile.separator +
        file.getName.replaceFirst("[.][^.]+$", "") + JFile.separator
    )

  /** A single file from the string path `f` using the supplied flags */
  def file(f: String)(implicit flags: TestFlags): TestInput = {
    val sourceFile = new JFile(f)
    assert(sourceFile.exists(), s"the file $f does not exist")
    TestInput(f, flags, outDir(sourceFile), sourceFile :: Nil)
  }

  /** A directory `f` using the supplied `flags`. This method does
   *  deep compilation, that is - it compiles all files and subdirectories
   *  contained within the directory `f`.
   */
  def directory(f: String, recursive: Boolean = true)(implicit flags: TestFlags): TestInput = {
    val sourceDir = new JFile(f)

    assert(sourceDir.exists(), s"the directory $f does not exist")

    def flatten(f: JFile): List[JFile] =
      if (f.isDirectory) {
        val files = f.listFiles.toList
        if (recursive) files.flatMap(flatten) else files
      }
      else List(f)

    val sortedFiles = flatten(sourceDir).sorted

    TestInput(f, flags, outDir(sourceDir), sortedFiles)
  }

  /** This function creates a list of TestInput for the files and folders
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
  def filesInDir(f: String)(implicit flags: TestFlags): List[TestInput] =
  {
    new JFile(f).listFiles.foldLeft(List.empty[TestInput]) { case (inputs, f) =>
      if (f.isDirectory) directory(f.getPath) :: inputs
      else file(f.getPath) :: inputs
    }
  }
}
