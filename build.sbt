ThisBuild / name         := "sbt-cc"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

Global / onChangedBuildSource := IgnoreSourceChanges

lazy val root = (project in file("."))
  .settings(
    name    := "sbt-cc",
    version := "0.1",
    organization := "com.github.tnakamot",
    scalaVersion := "2.12.10",
    sbtPlugin    := true,
    sbtVersion   := "1.3.9"
  )

