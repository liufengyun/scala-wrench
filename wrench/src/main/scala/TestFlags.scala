package org.xmid
package wrench

import java.io.{File => JFile}

final case class TestFlags private[wrench] (
  classPath: List[String],
  runClassPath: List[String], // class path that is used when running `run` tests (not compiling)
  pluginPath: List[String],
  runTimeout: Int,
  compileTimeout: Int,
  options: Map[String, String]) { // only valid compiler options can be stored directly in `options`

  def and(flag: String): TestFlags =
    copy(options = options + (flag -> ""))

  def and(pairs: Tuple2[String, String]*): TestFlags =
    copy(options = options ++ pairs)

  def without(flags: String*): TestFlags =
    copy(options = options -- flags)

  def withClassPath(path: String): TestFlags =
    copy(classPath = path :: this.classPath)

  def withClassPath(path: List[String]): TestFlags =
    copy(classPath = path ++ this.classPath)

  def withPluginPath(path: String): TestFlags =
    copy(pluginPath = path :: this.pluginPath)

  def withPluginPath(path: List[String]): TestFlags =
    copy(pluginPath = path ++ this.pluginPath)

  def withRunClassPath(path: String): TestFlags =
    copy(runClassPath = path :: this.runClassPath)

  def withRunClassPath(path: List[String]): TestFlags =
    copy(runClassPath = path ++ this.runClassPath)

  lazy val all: Array[String] =
    Array("-classpath", classPath.mkString(JFile.pathSeparator))
      ++ options.flatMap { case (k, v) =>
        if (v.length == 0) List(k) else List(k, v)
      }
      ++ {
        if (pluginPath.isEmpty) Nil
        else List("-Xplugin:" + pluginPath.mkString(","))
      }

  /** Subset of the flags that should be passed to javac. */
  def javacFlags: Array[String] = {
    val flags = all
    val cp = flags.dropWhile(_ != "-classpath").take(2)
    val output = flags.dropWhile(_ != "-d").take(2)
    cp ++ output
  }
}

object TestFlags {
  def apply(classPath: List[String], options: Map[String, String])(implicit ctx: TestContext): TestFlags =
    TestFlags(classPath, classPath, Nil, ctx.runTimeout, ctx.compileTimeout, options)
}
