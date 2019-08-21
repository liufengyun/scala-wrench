lazy val dottyVersion = "0.17.0-RC1"

def findLib(attList: Seq[Attributed[File]], name: String) =
  attList
    .map(_.data.getAbsolutePath)
    .find(_.contains(name))
    .toList.mkString(java.io.File.pathSeparator)

lazy val init: Project = project
  .in(file("init"))
  .settings(
    name := "init-checker",
    version := "0.0.1",
    organization := "xmid.org",
    scalaVersion := dottyVersion,
    fork := true,
    baseDirectory in Test := baseDirectory.value / "..",

    libraryDependencies ++= Seq(
      "ch.epfl.lamp" %% "dotty-compiler" % scalaVersion.value % "provided",
      "com.novocode" % "junit-interface" % "0.11" % "test"
    ),

    javaOptions in Test ++= {
      lazy val pluginJars = (Compile / packageBin / artifactPath).value

      List("-Dplugin=" + pluginJars)
    }
  )
  .dependsOn(wrench % Test)

lazy val wrench = project
  .in(file("wrench"))
  .settings(
    name := "scala-wrench",
    version := "0.0.1",
    organization := "xmid.org",
    scalaVersion := dottyVersion
  )
