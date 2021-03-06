import scala.collection.immutable.ListSet
import sbtcc._

ThisBuild / name         := "multi-execs"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"

val programA = Executable("programA")
val programB = Executable("programB")
val programC = Executable("programC")

lazy val multiExecs = (project in file("."))
  .enablePlugins(CcPlugin)
  .settings(
    Compile / ccTargets := ListSet(programA, programB, programC),
    Compile / ccRunExecutable := Some(programB),

    // ------------------------------------------------------------
    // This is the fallback settings. These flags will be used if
    // cFlags/ldFlags are not defined explicitly for the target.
    // ------------------------------------------------------------
    Compile / cFlags := (Compile / cFlags).value.withDefaultValue( Seq(
      "-Wall", "-Werror",
      "-DPROGRAM_NAME=\"" + name.value + "\"",
      "-DVERSION=\"" + version.value +"\"",
    )),
    Compile / ldFlags := (Compile / ldFlags).value.withDefaultValue( Seq( "-lm" ) ),

    // ------------------------------------------------------------
    // Compiler settings for Program A
    // ------------------------------------------------------------
    Compile / cSources ++= Map( programA -> Seq(
      baseDirectory.value / "program_a" / "main.c",
    )),

    // -------------------------------------------
    // Compiler settings for Program B
    // -------------------------------------------
    Compile / cSources ++= Map( programB -> Seq(
      baseDirectory.value / "program_b" / "main.c",
      baseDirectory.value / "program_b" / "util.c",
      baseDirectory.value / "program_b" / "util_aux.c",
    )),
    Compile / cFlags ++= Map(
      programB -> Seq( "-DPROGRAM_NAME=\"Program B\"", "-DVERSION=\"0.99.0\"" ),
    ),
    Compile / ldFlags ++= Map( programB -> Seq("-lm", "-lpthread") ),

    // -------------------------------------------
    // Compiler settings for Program C
    // -------------------------------------------
    Compile / cSources ++= Map( programC -> Seq(
      baseDirectory.value / "program_c" / "main.c",
      baseDirectory.value / "program_c" / "util.c",
      baseDirectory.value / "program_c" / "util_aux.c",
    )),
    Compile / cFlags ++= Map(
      programC -> Seq( "-DPROGRAM_NAME=\"Program C\"", "-DVERSION=\"0.0.1\"" ),
      (programC and (baseDirectory.value / "program_c" / "util_aux.c")) -> Seq( "-DPROGRAM_NAME=\"Program C Premium\"", "-DVERSION=\"0.0.1\"" ),
    ),

  )
