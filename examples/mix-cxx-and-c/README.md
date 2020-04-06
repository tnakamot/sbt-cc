# mix-cxx-and-c

This example sbt project how to use CcPlugin to compile C and C++ source files
and combine them into one target.

This example project also explains how to switch the commands of compiler, linker
and archiver.

## Mixing C and C++

Mixing C and C++ source files is not a big deal for CcPlugin. Just set all C and
C++ source files in `cSources` and `cxxSource` respectively as usual.  

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

The sbt code block above generates on executable `mixExe` from a C source file
`c_util.c` and a C++ source file `main.cxx`. In this example, the main function
in `main.cxx` calls a function defined in `c_util.c`.

When you expose your C functions, data structure and things to C++ world, do not
forget to surround the contents of every C header file with `extern "C" { ... }` as
shown in `c_util.h`. It prevents C++ compiler from mangling names for C things.

## Switching compiler, linker and archiver commands

CcPlugin simply calls a command when compiling C/C++ source files and when linking
or archiving object files. Those commands must be installed by yourself before
you actually compile the C/C++ source files.

The commands are specified in the following setting keys.

* cCompiler       : command to compile C source files [default: cc]
* cxxCompiler     : command to compile C++ source files [default: g++]
* ccArchiveCommand: command to archive object files and generate a static library [default: ar]
* ccLinkerCommand : Command to link object files and generate an executable or a shared library [default: g++]  

You can change the default values to your specific one if you do not have those
commands in your path. The code block in `build.sbt` shown below sets all
commands with absolute paths.

    Compile / cCompiler        := "/usr/bin/gcc",
    Compile / cxxCompiler      := "/usr/bin/g++",
    Compile / ccArchiveCommand := "/usr/bin/ar",
    Compile / ccLinkerCommand  := "/usr/bin/g++",


