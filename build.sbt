ThisBuild / name         := "sbt-cxx"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

Global / onChangedBuildSource := IgnoreSourceChanges

// List of example projects which show how to use CxxPlugin.
lazy val simpleC       = project in file("examples/simple-c")
lazy val compileCFlags = project in file("examples/compile-c-flags")

