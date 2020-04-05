import scala.collection.immutable.ListSet

ThisBuild / name         := "simple-c"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

val helloExe = Executable("hello")

lazy val simpleCxx = (project in file("."))
  .enablePlugins(CcPlugin)
  .settings(
    Compile / ccTargets := ListSet(helloExe),
    Compile / cxxSources  := Map(
      helloExe -> Seq(
        baseDirectory.value / "hello.cxx",
      )
    ),
    Compile / cxxFlags ++= Map(
      helloExe -> Seq( "-std=c++11" )
    ),
  )
