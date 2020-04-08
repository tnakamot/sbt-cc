import scala.collection.immutable.ListSet
import sbtcc._
import sbt.io.IO

ThisBuild / name         := "external-headers"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

val mainExe = Executable("main")

lazy val exampleHeaderGenerator = taskKey[Seq[File]]("Generate a header file.")
lazy val exampleSourceGenerator = taskKey[Seq[File]]("Generate a source file.")

lazy val externalHeaders = (project in file("."))
  .enablePlugins(CcPlugin)
  .settings(
    Compile / ccTargets := ListSet(mainExe),
    Compile / cSources  := Map(
      mainExe -> Seq(
        baseDirectory.value / "main.c",
      )
    ),

    // ---------------------------------------------------
    // An example to statically add header search paths.
    // ---------------------------------------------------
    Compile / cIncludes  := Map(
      mainExe -> Seq(
        file("/tmp/include"),
      )
    ),

    // -----------------------------------------
    // Example task to generate a header file.
    // -----------------------------------------
    Compile / exampleHeaderGenerator := {
      val header = (Compile / sourceManaged).value / "demo" / "dynamic.h"
      IO.write(header, """void dynamic_func();""")
      streams.value.log.info("Generated header: " + header.toString)
      Seq(header)
    },

    // ---------------------------------------------------
    // An example to dynamically add header search paths.
    // ---------------------------------------------------
    Compile / cIncludeDirectories := { CcPlugin.combineMaps(
      (Compile / cIncludes).value,
      Map( mainExe -> (Compile / exampleHeaderGenerator).value.map(_.getParentFile).distinct )
    ) },

    // -----------------------------------------
    // Example task to generate a source file.
    // -----------------------------------------
    Compile / exampleSourceGenerator := {
      val source = (Compile / sourceManaged).value / "demo" / "dynamic.c"
      IO.write(source,
        """
          #include <stdio.h>

          void dynamic_func(){
              printf("Hello from dynamic_func().\n");
           }
        """.stripMargin)
      streams.value.log.info("Generated header: " + source.toString)
      Seq(source)
    },

    // ---------------------------------------------------
    // An example to dynamically add header search paths.
    // ---------------------------------------------------
    Compile / cSourceFiles := { CcPlugin.combineMaps(
      (Compile / cSources).value,
      Map( mainExe -> (Compile / exampleSourceGenerator).value )
    ) },
  )
