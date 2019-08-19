package org.scalaverify
package test

import java.io.{ PrintStream, PrintWriter, File => JFile, FileOutputStream, StringWriter }
import java.text.SimpleDateFormat
import java.util.Date
import dotty.tools.dotc.core.Decorators._

import scala.collection.mutable

import dotty.tools.dotc.util.SourcePosition
import dotty.tools.dotc.core.Contexts._
import dotty.tools.dotc.reporting._
import dotty.tools.dotc.reporting.diagnostic.{ Message, MessageContainer, NoExplanation }
import dotty.tools.dotc.reporting.diagnostic.messages._
import dotty.tools.dotc.interfaces.Diagnostic.{ ERROR, WARNING, INFO }

class TestReporter protected (outWriter: PrintWriter, logLevel: Int)
extends Reporter with UniqueMessagePositions with MessageRendering {
  import MessageContainer._

  protected final val _errorBuf = mutable.ArrayBuffer.empty[MessageContainer]
  final def errors: Iterator[MessageContainer] = _errorBuf.iterator

  protected final val _messageBuf = mutable.ArrayBuffer.empty[String]
  final def messages: Iterator[String] = _messageBuf.iterator

  protected final val _consoleBuf = new StringWriter
  protected final val _consoleReporter = new ConsoleReporter(null, new PrintWriter(_consoleBuf))
  final def consoleOutput: String = _consoleBuf.toString

  private[this] var _didCrash = false
  final def compilerCrashed: Boolean = _didCrash

  protected final def inlineInfo(pos: SourcePosition)(implicit ctx: Context): String =
    if (pos.exists) {
      if (pos.outer.exists)
        i"\ninlined at ${pos.outer}:\n" + inlineInfo(pos.outer)
      else ""
    }
    else ""

  /** Prints the message with the given position indication. */
  def printMessageAndPos(m: MessageContainer, extra: String)(implicit ctx: Context): Unit = {
    val msg = messageAndPos(m.contained(), m.pos, diagnosticLevel(m))
    val extraInfo = inlineInfo(m.pos)

    if (m.level >= logLevel) {
      outWriter.println(msg)
      if (extraInfo.nonEmpty) outWriter.println(extraInfo)
    }

    _messageBuf.append(msg)
    if (extraInfo.nonEmpty) _messageBuf.append(extraInfo)
  }

  override def doReport(m: MessageContainer)(implicit ctx: Context): Unit = {

    // Here we add extra information that we should know about the error message
    val extra = m.contained() match {
      case pm: PatternMatchExhaustivity => s": ${pm.uncovered}"
      case _ => ""
    }

    m match {
      case m: Error => {
        _errorBuf.append(m)
        _consoleReporter.doReport(m)
        printMessageAndPos(m, extra)
      }
      case m =>
        printMessageAndPos(m, extra)
    }
  }
}

object TestReporter {
  def reporter(ps: PrintStream, logLevel: Int): TestReporter =
    new TestReporter(new PrintWriter(ps, true), logLevel)
}
