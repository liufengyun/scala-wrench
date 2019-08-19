lazy val dottyVersion = "0.17.0-RC1"

def findLib(attList: Seq[Attributed[File]], name: String) =
  attList
    .map(_.data.getAbsolutePath)
    .find(_.contains(name))
    .toList.mkString(java.io.File.pathSeparator)

lazy val plugin: Project = project
  .in(file("plugin"))
  .settings(
    name := "scala-verify",
    version := "0.0.1",
    organization := "org.scalaverify",
    scalaVersion := dottyVersion,
    fork := true,
    baseDirectory in Test := baseDirectory.value / "..",

    libraryDependencies ++= Seq(
      "ch.epfl.lamp" %% "dotty-compiler" % scalaVersion.value % "provided",
      "com.novocode" % "junit-interface" % "0.11" % "test"
    ),

    javaOptions in Test ++= {
      val attList = (dependencyClasspath in Runtime).value
      lazy val pluginJars = (Compile / packageBin / artifactPath).value

      List(
        "-DdottyInterfaces=" + findLib(attList, "dotty-interfaces"),
        "-DdottyLibrary=" + findLib(attList, "dotty-library"),
        "-DdottyCompiler=" + findLib(attList, "dotty-compiler"),
        "-DcompilerInterface=" + findLib(attList, "compiler-interface"),
        "-DscalaLibrary=" + findLib(attList, "scala-library-"),
        "-DscalaAsm=" + findLib(attList, "scala-asm"),
        "-DjlineTerminal=" + findLib(attList, "jline-terminal"),
        "-DjlineReader=" + findLib(attList, "jline-reader"),
        "-Dplugin=" + pluginJars
      )
    }
  )

lazy val app = project
  .in(file("app"))
  .settings(
    scalaVersion := dottyVersion
  )
