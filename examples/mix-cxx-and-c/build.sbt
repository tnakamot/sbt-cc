import scala.collection.immutable.ListSet
import sbtcc._

ThisBuild / name         := "mix-cxx-and-c"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

val mixExe = Executable("mix")

lazy val mixCxxAndC = (project in file("."))
  .enablePlugins(CcPlugin)
  .settings(
    Compile / cCompiler        := "/usr/bin/gcc",
    Compile / cxxCompiler      := "/usr/bin/g++",
    Compile / ccArchiveCommand := "/usr/bin/ar",
    Compile / ccLinkerCommand  := "/usr/bin/g++",

    Compile / ccTargets := ListSet(mixExe),
    Compile / cSources  := Map(
      mixExe -> Seq(
        baseDirectory.value / "c_util.c",
      )
    ),
    Compile / cxxSources  := Map(
      mixExe -> Seq(
        baseDirectory.value / "main.cxx",
      )
    ),
  )
