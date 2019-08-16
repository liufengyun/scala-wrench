lazy val dottyVersion = "0.17.0-RC1"

lazy val plugin = project
  .in(file("plugin"))
  .settings(
    name := "scala-verify",
    version := "0.0.1",
    organization := "org.scalaverify",
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
