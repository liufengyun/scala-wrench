package analysis
package util

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

class DefTreeChecker extends PluginPhase {
  import tpd._

  val phaseName = "defTreeChecker"

  override val runsAfter = Set(SetDefTree.name)
  override val runsBefore = Set(CheckStatic.name)

  private def checkDef(tree: Tree)(implicit ctx: Context): Tree = {
    if (tree.symbol.defTree.isEmpty)
      ctx.error("cannot get tree for " + tree.show, tree.sourcePos)
    tree
  }

  private def checkable(sym: Symbol)(implicit ctx: Context): Boolean =
    sym.exists && !sym.isOneOf(Flags.Package) && !sym.isOneOf(Flags.Param) &&
      (sym.isClass || !sym.isOneOf(Flags.Case, butNot = Flags.Enum)) // pattern-bound symbols

  private def checkRef(tree: Tree)(implicit ctx: Context): Tree =
    if (!checkable(tree.symbol)) tree
    else {
      val helloPkgSym = ctx.requiredPackage("hello").moduleClass
      val libPkgSym = ctx.requiredPackage("lib").moduleClass
      val enclosingPkg = tree.symbol.enclosingPackageClass

      if (enclosingPkg == helloPkgSym) {  // source code
        checkDef(tree)
        ctx.warning("tree: " + tree.symbol.defTree.show)
      }
      else if (enclosingPkg == libPkgSym) { // tasty from library
        checkDef(tree)
        // check that all sub-definitions have trees set properly
        // make sure that are no cycles in the code
        transformAllDeep(tree.symbol.defTree)
        ctx.warning("tree: " + tree.symbol.defTree.show)
      }
      else {
        ctx.warning(tree.symbol + " is neither in lib nor hello, owner = " + enclosingPkg, tree.sourcePos)
      }
      tree
    }

  override def transformValDef(tree: ValDef)(implicit ctx: Context): Tree = checkDef(tree)

  override def transformDefDef(tree: DefDef)(implicit ctx: Context): Tree = checkDef(tree)

  override def transformTypeDef(tree: TypeDef)(implicit ctx: Context): Tree = checkDef(tree)

  override def transformSelect(tree: Select)(implicit ctx: Context): Tree = checkRef(tree)

  override def transformIdent(tree: Ident)(implicit ctx: Context): Tree = checkRef(tree)

  override def transformTypeTree(tree: TypeTree)(implicit ctx: Context): Tree = {
    tree.tpe.foreachPart {
      case tp: NamedType => checkRef(TypeTree(tp))
      case _ =>
    }
    tree
  }
}