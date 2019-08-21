package org.xmid
package wrench

import java.io.{File => JFile}
import java.nio.file._

import wrench.Util._

def file(f: String)(implicit flags: TestFlags): TestCase = TestCase.file(f)
def directory(f: String)(implicit flags: TestFlags): TestCase = TestCase.directory(f)
def testsIn(f: String)(implicit flags: TestFlags): List[TestCase] = TestCase.testsIn(f)

def (test: TestCase) shouldCompile(implicit ctx: TestContext): Unit = {
  if (test.compile.checkSucceeded) ctx.passed(test)
  else ctx.failed(test)
  test.cleanup
}

def (test: TestCase) checkCompile(implicit ctx: TestContext): Unit = {
  if (test.compile.checkCompile) ctx.passed(test)
  else ctx.failed(test)
  test.cleanup
}

def (test: TestCase) shouldRun(implicit ctx: TestContext): Unit = {
  if (test.compile.checkSucceeded && test.run.checkSucceeded) ctx.passed(test)
  else ctx.failed(test)
  test.cleanup
}

def (tests: List[TestCase]) shouldCompile(implicit ctx: TestContext): Unit = tests.map(_.shouldCompile)
def (tests: List[TestCase]) checkCompile(implicit ctx: TestContext): Unit = tests.map(_.checkCompile)
def (tests: List[TestCase]) shouldRun(implicit ctx: TestContext): Unit = tests.map(_.shouldRun)

def withPlugin(paths: String*)(op: given TestFlags => Unit)(implicit flags: TestFlags, ctx: TestContext): Unit = {
  val pluginOuts = paths.toList.map { path =>
    val flag2 = flags.withClassPath(Defaults.compilerClasspath)
    directory(path)(flag2).compile
  }
  val success = pluginOuts.forall { pluginOut =>
    val success = pluginOut.checkSucceeded
    if (!success) ctx.failed(pluginOut.test)
    success
  }

  if (!success) return

  val pluginClassPaths = pluginOuts.map {
    case CompileOutput(test: DirectoryTestCase, _, _)  =>
      // copy plugin.properties
      val propFile = "plugin.properties"
      val source = test.sourceDir.child(propFile)
      val dest = test.targetDir.child(propFile)
      Files.copy(source.toPath, dest.toPath, StandardCopyOption.REPLACE_EXISTING)
      test.targetDir.getAbsolutePath
    case _ =>
      ??? // impossible
  }

  val flags2 = flags.withPluginPath(pluginClassPaths)
  op given flags2

  pluginOuts.foreach(_.test.cleanup)
}

/** Compile with pre-existing */
def withPluginBin(paths: String)(op : given TestFlags => Unit)(implicit flags: TestFlags, ctx: TestContext): Unit = {
  val flags2 = flags.withPluginPath(paths)
  op given flags2
}