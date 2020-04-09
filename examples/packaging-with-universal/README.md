# packaging-with-universal

This is an example sbt project to show how you can use CcPlugin to compile
C source files and package the targets (executables, static and shared libraries)
using
[Unviersal plugin](https://www.scala-sbt.org/sbt-native-packager/formats/universal.html).

Through this example, you will learn how to pass the targets of CcPlugin to
the other tasks or plugins.

As usual, we first need to define targets in `build.sbt`. In this example,
one executable, one static library and one shared library are defined as targets
as can be seen in the excerpt of `build.sbt` below:

    val helloExe = Executable("hello")
    val exampleStaticLib = Library("libexample.a")
    val exampleSharedLib = SharedLibrary("libexample.so")

    ...

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
    
The output paths of the targets can be obtained by `ccLinkExecutables` task for
executables, `ccLinkLibraries` task for static libraries and `ccLinkSharedLibraries`
task for shared libraries. These tasks actually link object files to make the 
targets, but that happens only when any of the object files is newer than the 
target. So, invoking these tasks many times do not burden your system. They return
an instance of `Seq[File]` which contains file paths of the targets.

So, you can simply pass the return value of those tasks to the others. In this
example, they are passed to Universal plugin as shown below.
    
    mappings in Universal ++= (Compile / ccLinkExecutables).value map { exe =>
      exe -> ("bin/" + exe.getName)
    },
    mappings in Universal ++= (Compile / ccLinkLibraries).value map { lib =>
      lib -> ("lib/" + lib.getName)
    },
    mappings in Universal ++= (Compile / ccLinkSharedLibraries).value map { lib =>
      lib -> ("lib/" + lib.getName)
    },

With the above settings, when you run `sbt stage`, Universal Plugin copies the
executables to `target/universal/stage/bin` and the libraries to
`target/universal/stage/lib`. For details about the usage of Universal Plugin,
please visit
[its website](https://www.scala-sbt.org/sbt-native-packager/formats/universal.html).
