import java.nio.file.{Files, Path}

import sbt.Keys._
import sbt._
import complete.DefaultParsers._
import sbt.internal.util.ManagedLogger

import scala.sys.process._
import scala.util.Try

trait Target{
  val name: String
  def and(file: File): Target
}
trait Source{val file: File}

case class ProgramAndSource(val name: String, val file: File) extends Target with Source {
  def and(file: File) = {this}
}
case class LibraryAndSource(val name: String, val file: File) extends Target with Source {
  def and(file: File) = {this}
}
case class SharedLibraryAndSource(val name: String, val file: File) extends Target with Source {
  def and(file: File) = {this}
}

case class Program(val name: String) extends Target { // Target which represents an executable.
  def and(file: File): ProgramAndSource = {ProgramAndSource(name, file)}
}
case class Library(val name: String) extends Target { // Target which represents a static library (*.a).
  def and(file: File): LibraryAndSource = {LibraryAndSource(name, file)}
}
case class SharedLibrary(val name: String) extends Target { // Target which represents a shared library (*.so).
  def and(file: File): SharedLibraryAndSource = {SharedLibraryAndSource(name, file)}
}

object CcPlugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    lazy val ccTargets = settingKey[Set[Target]]("Targets")

    lazy val cCompiler    = settingKey[String]("Command to compile C source files.")
    lazy val cFlags       = settingKey[Map[Target,Seq[String]]]("Flags to be passed to the C compiler.") // We may want to use different flags for each source file.
    lazy val cSourceFiles = settingKey[Map[Target,Seq[File]]]("Path of C source files.")
    lazy val cSourceFilesDynamic = taskKey[Map[Target,Seq[File]]]("Path of C source files (dynamically defined).")

    lazy val cCompile     = inputKey[Seq[File]]("Input task to compile C source files for a specific output.")
    lazy val cCompileAll  = taskKey[Seq[File]]("Task to compile C source files for all the targets.")

    lazy val cxxCompiler    = settingKey[String]("Command to compile C++ source files.")
    lazy val cxxFlags       = settingKey[Map[Target,Seq[String]]]("Flags to be passed to the C++ compiler.")
    lazy val cxxSourceFiles = settingKey[Map[Target,Seq[File]]]("Path of C++ source files.")
    lazy val cxxSourceFilesDynamic = taskKey[Map[Target,Seq[File]]]("Path of C++ source files (dynamically defined).")

    lazy val ccSourceObjectMap = taskKey[Map[(Configuration, Target, File), File]]("Mapping from source files to object files.")
  }
  import autoImport._

  def isDescendent(directory: File, file: File): Boolean = {
    if (!directory.isDirectory) {
      throw new Exception(directory.toString + " is not a directory.")
    }

    file.toPath.startsWith(directory.toPath.toAbsolutePath)
  }

  def compile(compiler: String,
              flags: Map[Target,Seq[String]],
              config: Configuration,
              targs : Set[Target],
              sources: Map[Target,Seq[File]],
              srcObjMap: Map[(Configuration, Target, File), File],
              logger: ManagedLogger
             ): Seq[File] = {
    var objs: List[File] = List()
    for (targ <- targs) {
      for (source <- sources.getOrElse(targ, Seq())) {
        val obj = srcObjMap((config, targ, source))
        val srcFlags = flags.getOrElse(targ and source, Try(flags(targ)).getOrElse(Seq()))
        val cmd: Seq[String] = Seq(compiler, "-c", source.toString, "-o", obj.toString) ++ srcFlags
        val p = Process(cmd)
        logger.info(p.toString)

        Files.createDirectories(obj.toPath.getParent)
        Process(cmd).!

        objs ++= List(obj)
      }
    }
    objs
  }

  override lazy val projectSettings = Seq(
    Compile / ccTargets := Set(),
    Test    / ccTargets := Set(),

    cCompiler   := "cc",
    cxxCompiler := "g++",

    Compile / cFlags   := Map(),
    Test    / cFlags   := Map(),
    Compile / cxxFlags := Map(),
    Test    / cxxFlags := Map(),

    Compile / cSourceFiles   := Map(),
    Test    / cSourceFiles   := Map(),
    Compile / cxxSourceFiles := Map(),
    Test    / cxxSourceFiles := Map(),

    Compile / cSourceFilesDynamic   := { (Compile / cSourceFiles  ).value },
    Test    / cSourceFilesDynamic   := { (Test    / cSourceFiles  ).value },
    Compile / cxxSourceFilesDynamic := { (Compile / cxxSourceFiles).value },
    Test    / cxxSourceFilesDynamic := { (Test    / cxxSourceFiles).value },

    Compile / cCompile := {
      val targetNames: Seq[String] = spaceDelimited("<args>").parsed
      val compileTargets = (Compile / ccTargets).value.filter(t => targetNames.contains(t.name))

      compile(
        (Compile / cCompiler).value,
        (Compile / cFlags).value,
        Compile,
        compileTargets,
        (Compile / cSourceFilesDynamic).value,
        ccSourceObjectMap.value,
        streams.value.log,
      )
    },

    ccSourceObjectMap := {
      val compileCSources   = ((Compile / cSourceFilesDynamic  ).value.map{ case (targ, files) => files.map((Compile, targ, _)) } toList).flatten
      val compileCxxSources = ((Compile / cxxSourceFilesDynamic).value.map{ case (targ, files) => files.map((Compile, targ, _)) } toList).flatten
      val testCSources      = ((Test    / cSourceFilesDynamic  ).value.map{ case (targ, files) => files.map((Test   , targ, _)) } toList).flatten
      val testCxxSources    = ((Test    / cxxSourceFilesDynamic).value.map{ case (targ, files) => files.map((Test   , targ, _)) } toList).flatten

      if (compileCSources.exists(compileCxxSources.contains)) {
        throw new Exception("(Compile / cSourceFilesDynamic) and (Compile in cxxSourceFilesDynamic) have common source files for the same target.")
      }

      if (testCSources.exists(testCxxSources.contains)) {
        throw new Exception("(Test / cSourceFilesDynamic) and (Test in cxxSourceFilesDynamic) have common source files for the same target.")
      }

      val cacheDirectory = streams.value.cacheDirectory
      val ret = Seq(compileCSources,
        compileCxxSources,
        testCSources,
        testCxxSources).flatten.map { case (config, targ, src) =>
        val obj: File = if (isDescendent(baseDirectory.value, src)) {
          cacheDirectory / config.name / targ.name / ((baseDirectory.value.toPath.relativize(src.toPath)).toString + ".o")
        } else {
          val dirHash = f"${src.getParent.hashCode}%08x"
          cacheDirectory / config.name / targ.name / ("_" + dirHash) / (src.getName + ".o")

          // TODO: By any chance the generated file name may conflict with another.
          //       If conflict, change the dirHash to another.
        }

        ((config, targ, src), obj)
      }.toMap

      ret
    }
  )
}
