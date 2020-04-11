Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .enablePlugins(GitVersioning)
  .settings(
    name    := "sbt-cc",
    organization := "com.github.tnakamot",
    description  := "CcPlugin: sbt plugin to compile C and C++ source files",
    licenses     += ("GPL-3.0", url("https://www.gnu.org/licenses/gpl-3.0.en.html")),
    scalaVersion := "2.12.10",
    sbtPlugin    := true,
    sbtVersion   := "1.3.9",

    publishMavenStyle := false,
    bintrayReleaseOnPublish in ThisBuild := false,
  )


