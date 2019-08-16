package analysis

import dotty.tools.dotc.plugins._

class Plugin extends StandardPlugin {
  val name: String = "checker"
  override val description: String = "initialization checker"

  def init(options: List[String]): List[PluginPhase] =
    (new util.SetDefTree) :: (new analysis.init.Checker) :: Nil
}
