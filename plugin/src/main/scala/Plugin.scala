package org.scalaverify

import dotty.tools.dotc.plugins._
import org.scalaverify.init.Checker

class Plugin extends StandardPlugin {
  val name: String = "checker"
  override val description: String = "initialization checker"

  def init(options: List[String]): List[PluginPhase] =
    (new util.SetDefTree) :: (new Checker) :: Nil
}
