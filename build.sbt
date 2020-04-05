ThisBuild / name         := "sbt-cc"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

Global / onChangedBuildSource := IgnoreSourceChanges

lazy val root = (project in file("."))
  .settings(
    name    := "sbt-cc",
    version := "0.1",
    organization := "com.github.nakamot",
    scalaVersion := "2.12.10",
    sbtPlugin    := true,
    sbtVersion   := "1.3.9"
  )

// List of example projects which show how to use CxxPlugin.
lazy val simpleC    = project in file("examples/simple-c")
lazy val multiExecs = project in file("examples/multi-execs")
lazy val simpleCxx  = project in file("examples/simple-cxx")


