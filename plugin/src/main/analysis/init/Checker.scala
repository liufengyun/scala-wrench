package analysis
package init

import scala.language.implicitConversions

import dotty.tools.dotc._
import core._
import Contexts.Context
import plugins._
import Phases.Phase
import ast.tpd
import transform.MegaPhase.MiniPhase
import Decorators._
import Symbols.Symbol
import Constants.Constant
import Types._
import transform.{CheckStatic}

import analysis.util._

class Checker extends PluginPhase {
  import tpd._

  val phaseName = "initChecker"

  override val runsAfter = Set(SetDefTree.name)
  override val runsBefore = Set(CheckStatic.name)
}