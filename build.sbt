import xerial.sbt.Sonatype._

lazy val dottyVersion = "0.17.0-RC1"

lazy val wrench = project
  .in(file("wrench"))
  .settings(
    name := "scala-wrench",
    version := "0.0.1",
    organization := "org.xmid",
    scalaVersion := dottyVersion,
    fork := true,  // important

    baseDirectory in Test := baseDirectory.value / "..",

    publishTo := sonatypePublishTo.value,

    publishMavenStyle := true,
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(GitHubHosting("liufengyun", "scala-wrench", "fengyun@xmid.org")),

    libraryDependencies ++= Seq(
      "ch.epfl.lamp" %% "dotty-compiler" % dottyVersion % "test",
      "com.novocode" % "junit-interface" % "0.11" % "test"
    )
  )
