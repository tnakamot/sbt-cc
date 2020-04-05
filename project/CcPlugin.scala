import java.nio.file.Files

import sbt.Keys._
import sbt._
import complete.DefaultParsers._
import sbt.internal.util.ManagedLogger

import scala.collection.immutable.ListSet
import scala.sys.process._
import scala.util.Try

trait Target{
  val name: String
  def and(file: File): Target
}
trait Source{val file: File}

case class ProgramAndSource(name: String, file: File) extends Target with Source {
  def and(file: File) = this
}
case class LibraryAndSource(name: String, file: File) extends Target with Source {
  def and(file: File) = this
}
case class SharedLibraryAndSource(name: String, file: File) extends Target with Source {
  def and(file: File) = this
}

case class Program(name: String) extends Target { // Target which represents an executable.
  def and(file: File): ProgramAndSource = {ProgramAndSource(name, file)}
}
case class Library(name: String) extends Target { // Target which represents a static library (*.a).
  def and(file: File): LibraryAndSource = {LibraryAndSource(name, file)}
}
case class SharedLibrary(name: String) extends Target { // Target which represents a shared library (*.so).
  def and(file: File): SharedLibraryAndSource = {SharedLibraryAndSource(name, file)}
}

object CcPlugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    lazy val ccTargets         = settingKey[ListSet[Target]]("Targets")

    lazy val cCompiler    = settingKey[String]("Command to compile C source files.")
    lazy val cFlags       = settingKey[Map[Target,Seq[String]]]("Flags to be passed to the C compiler.") // We may want to use different flags for each source file.
    lazy val cSourceFiles = settingKey[Map[Target,Seq[File]]]("Path of C source files.")
    lazy val cSourceFilesDynamic = taskKey[Map[Target,Seq[File]]]("Path of C source files (dynamically defined).")

    lazy val cCompile     = inputKey[Seq[File]]("Input task to compile C source files for a specific output.")

    lazy val cxxCompiler    = settingKey[String]("Command to compile C++ source files.")
    lazy val cxxFlags       = settingKey[Map[Target,Seq[String]]]("Flags to be passed to the C++ compiler.")
    lazy val cxxSourceFiles = settingKey[Map[Target,Seq[File]]]("Path of C++ source files.")
    lazy val cxxSourceFilesDynamic = taskKey[Map[Target,Seq[File]]]("Path of C++ source files (dynamically defined).")

    lazy val ccArchiveCommand = settingKey[String]("Command to archive object files.")

    lazy val ldFlags = settingKey[Map[Target,Seq[String]]]("Flags to be passed to the compiler when linking.")
    lazy val ccLinkOnly    = inputKey[Seq[File]]("Input task to link object files and generate executable programs, static libraries and shared libraries. Does not compile dependent C source files.")
    lazy val ccLink        = inputKey[Seq[File]]("Input task to link object files and generate executable programs, static libraries and shared libraries.")

    lazy val ccSourceObjectMap = taskKey[Map[(Configuration, Target, File), File]]("Mapping from source files to object files.")
    lazy val ccTargetMap       = taskKey[Map[Target,File]]("Mapping from target names to the output paths of the targets.")

    lazy val ccRunProgram = settingKey[Option[Program]]("Program to launch when 'run' command is issued.")
  }
  import autoImport._

  def isDescendent(directory: File, file: File): Boolean = {
    if (!directory.isDirectory) {
      throw new Exception(directory.toString + " is not a directory.")
    }

    file.toPath.startsWith(directory.toPath.toAbsolutePath)
  }

  def cccompile(compiler: String,
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
        val compileFlags = flags.getOrElse(targ and source, Try(flags(targ)).getOrElse(Seq()))
        val cmd: Seq[String] = Seq(compiler, "-c", source.toString, "-o", obj.toString) ++ compileFlags
        val p = Process(cmd)
        logger.info(p.toString)

        // TODO: compile only when the object file is older than the source file.

        // TODO: compile if the dependent header files are newer than the source file.

        Files.createDirectories(obj.toPath.getParent)
        p.!!

        objs ++= List(obj)
      }
    }
    objs
  }

  def link(compiler: String,
           archiver: String,
           flags: Map[Target,Seq[String]],
           config: Configuration,
           targs : Set[Target],
           sources: Map[Target,Seq[File]],
           srcObjMap: Map[(Configuration, Target, File), File],
           logger: ManagedLogger,
           targetMap: Map[Target, File],
          ): Seq[File] = {
    targs.map( targ => {
      val objs = sources.getOrElse(targ, Seq()).map(srcObjMap(config, targ, _))
      val ldflags = Try(flags(targ)).getOrElse(Seq())
      val output = targetMap(targ)

      val cmd: Seq[String] = targ match {
        case Program(_) => Seq(compiler, "-o", output.toString) ++ objs.map(_.toString) ++ ldflags
        case Library(_) => Seq(archiver, "cr", output.toString) ++ objs.map(_.toString) ++ ldflags
        case SharedLibrary(_) => Seq(compiler, "-shared", "-o", output.toString) ++ objs.map(_.toString) ++ ldflags
      }

      val p = Process(cmd)
      logger.info(p.toString)

      // TODO: link only when the executable program is older than the object files.

      Files.createDirectories(output.getParentFile.toPath)
      p.!!

      output
    }).toSeq
  }

  def pickTarget(targs: Set[Target], names: Seq[String], logger: ManagedLogger): Set[Target] = {
    val invalidNames = names.filter(name => ! targs.map(_.name).contains(name))
    if (invalidNames.nonEmpty)
      logger.warn("Following targets in ccTargets do not exist: " + invalidNames.mkString(", "))

    if (names.isEmpty)
      targs
    else
      targs.filter(t => names.contains(t.name))
  }

  override lazy val projectSettings = Seq(
    Compile / ccTargets := ListSet(),
    Test    / ccTargets := ListSet(),

    Compile / ccRunProgram := Try(Some((Compile / ccTargets).value.filter(t => t.isInstanceOf[Program]).map(t => Program(t.name)).last)).getOrElse(None),
    Test    / ccRunProgram := Try(Some((Test    / ccTargets).value.filter(t => t.isInstanceOf[Program]).map(t => Program(t.name)).last)).getOrElse(None),

    cCompiler   := "cc",
    cxxCompiler := "g++",
    ccArchiveCommand := "ar",

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

    Compile / ldFlags := Map(),
    Test    / ldFlags := Map(),

    // TODO: define similar tasks for C++ and Test configuration.
    Compile / cCompile := {
      val targetNames: Seq[String] = spaceDelimited("<args>").parsed
      val compileTargets = pickTarget((Compile / ccTargets).value, targetNames, streams.value.log)

      println("cCompile: " + targetNames.mkString(" "))

      cccompile(
        (Compile / cCompiler).value,
        (Compile / cFlags).value,
        Compile,
        compileTargets,
        (Compile / cSourceFilesDynamic).value,
        ccSourceObjectMap.value,
        streams.value.log,
      )
    },

    Compile / ccLinkOnly := {
      val targetNames: Seq[String] = spaceDelimited("<args>").parsed
      val linkTargets = pickTarget((Compile / ccTargets).value, targetNames, streams.value.log)

      link(
        (Compile / cCompiler).value,
        (Compile / ccArchiveCommand).value,
        (Compile / ldFlags).value,
        Compile,
        linkTargets,
        (Compile / cSourceFilesDynamic).value,
        ccSourceObjectMap.value,
        streams.value.log,
        ccTargetMap.value,
      )
    },

    Compile / ccLink := Def.inputTaskDyn {
      val args: Seq[String] = spaceDelimited("<args>").parsed
      Def.taskDyn {
        (Compile / cCompile).toTask(" " + args.mkString(" ")).value
        (Compile / ccLinkOnly).toTask(" " + args.mkString(" "))
      }
    }.evaluated,

    (Compile / compile) := ((Compile / compile) dependsOn (Compile / ccLink).toTask("")).value,

    (Compile / run)     := {
      val args: Seq[String] = spaceDelimited("<args>").parsed
      val program: Program = (Compile / ccRunProgram).value match {
        case Some(p) => p
        case None => throw new Exception("ccRunProgram is not defined.")
      }

      // TODO: compile and  make the program first here.

      val programPath = ccTargetMap.value(program)
      val process = Process(Seq(programPath.toString) ++ args)
      streams.value.log.info(process.toString)

      process.!
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
          cacheDirectory / config.name / targ.name / (baseDirectory.value.toPath.relativize(src.toPath).toString + ".o")
        } else {
          val dirHash = f"${src.getParent.hashCode}%08x"
          cacheDirectory / config.name / targ.name / ("_" + dirHash) / (src.getName + ".o")

          // TODO: By any chance the generated file name may conflict with another.
          //       If conflict, change the dirHash to another or raise an Exception.
        }

        ((config, targ, src), obj)
      }.toMap

      ret
    },

    ccTargetMap := {
      val cacheDirectory = streams.value.cacheDirectory
      (Compile / ccTargets).value.map(t => t -> (cacheDirectory / Compile.name / t.getClass.getName / t.name)).toMap ++
        (Test  / ccTargets).value.map(t => t -> (cacheDirectory / Test.name    / t.getClass.getName / t.name)).toMap
    },
  )

}
