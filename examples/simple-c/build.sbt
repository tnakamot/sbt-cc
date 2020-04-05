import scala.collection.immutable.ListSet
import sbtcc._

ThisBuild / name         := "simple-c"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

val helloExe = Executable("hello")

lazy val simpleC = (project in file("."))
  .enablePlugins(CcPlugin)
  .settings(
    Compile / ccTargets := ListSet(helloExe),
    Compile / cSources  := Map(
      helloExe -> Seq(
        baseDirectory.value / "hello.c",
        baseDirectory.value / "hello_util.c"
      )
    ),
  )
