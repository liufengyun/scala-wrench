package org.xmid
package wrench

import java.io.{File => JFile}
import java.nio.file._

import wrench.Util._

def file(f: String)(implicit flags: TestFlags): TestCase = TestCase.file(f)
def directory(f: String)(implicit flags: TestFlags): TestCase = TestCase.directory(f)
def testsIn(f: String)(implicit flags: TestFlags): List[TestCase] = TestCase.testsIn(f)

def (test: TestCase) shouldCompile(implicit ctx: TestContext): Unit = ctx.transaction {
  if (test.compile.checkSucceeded) ctx.passed(test)
  else ctx.failed(test)
  test.cleanup
}

def (test: TestCase) checkCompile(implicit ctx: TestContext): Unit = ctx.transaction {
  if (test.compile.checkCompile) ctx.passed(test)
  else ctx.failed(test)
  test.cleanup
}

def (test: TestCase) shouldRun(implicit ctx: TestContext): Unit = ctx.transaction {
  if (test.compile.checkSucceeded && test.run.checkSucceeded) ctx.passed(test)
  else ctx.failed(test)
  test.cleanup
}

def (tests: List[TestCase]) shouldCompile(implicit ctx: TestContext): Unit = tests.parallelize(_.shouldCompile)
def (tests: List[TestCase]) checkCompile(implicit ctx: TestContext): Unit = tests.parallelize(_.checkCompile)
def (tests: List[TestCase]) shouldRun(implicit ctx: TestContext): Unit = tests.parallelize(_.shouldRun)

def withPlugin(paths: String*)(op: given TestFlags => Unit)(implicit flags: TestFlags, ctx: TestContext): Unit = {
  var success = true
  val pluginOuts = paths.toList.map { path =>
    val flag2 = flags.withClassPath(Defaults.compilerClasspath)
    val test = directory(path)(flag2)

    var compileOut: CompileOutput  = null
    ctx.transaction {
      compileOut = test.compile
      if (!compileOut.checkSucceeded) {
        success = false
        ctx.failed(test)
      }
    }

    compileOut
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