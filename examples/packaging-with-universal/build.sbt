import scala.collection.immutable.ListSet
import sbtcc._

ThisBuild / name         := "simple-c"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

val helloExe = Executable("hello")
val exampleStaticLib = Library("libexample.a")
val exampleSharedLib = SharedLibrary("libexample.so")

lazy val packagingWithUniversal = (project in file("."))
  .enablePlugins(CcPlugin, UniversalPlugin)
  .settings(
    Compile / ccTargets := ListSet(helloExe, exampleStaticLib, exampleSharedLib),
    Compile / cSources  := Map(
      helloExe -> Seq(
        baseDirectory.value / "hello.c",
      ),
      exampleStaticLib -> Seq(
        baseDirectory.value / "example_lib.c"
      ),
      exampleSharedLib -> Seq(
        baseDirectory.value / "example_lib.c"
      ),
    ),

    mappings in Universal ++= (Compile / ccLinkExecutables).value map { exe =>
      exe -> ("bin/" + exe.getName)
    },
    mappings in Universal ++= (Compile / ccLinkLibraries).value map { lib =>
      lib -> ("lib/" + lib.getName)
    },
    mappings in Universal ++= (Compile / ccLinkSharedLibraries).value map { lib =>
      lib -> ("lib/" + lib.getName)
    },
    mappings in Universal +=  (baseDirectory.value / "example_lib.h") -> "include/example_lib.h"
  )
