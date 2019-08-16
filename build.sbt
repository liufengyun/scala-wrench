lazy val dottyVersion = "0.17.0-RC1"

lazy val plugin = project
  .in(file("plugin"))
  .settings(
    name := "init-checker",
    version := "0.0.1",
    organization := "ch.epfl.lamp",
    scalaVersion := dottyVersion,

    libraryDependencies ++= Seq(
      "ch.epfl.lamp" %% "dotty-compiler" % scalaVersion.value % "provided"
    )
  )

lazy val app = project
  .in(file("app"))
  .settings(
    scalaVersion := dottyVersion
  )
