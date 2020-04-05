# simple-c

This is an example sbt project to show the simplest usage of CxxPlugin.

This project has two source files (`hello.c` and `hello_util.c`) and one header
file (`hello_util.h`). The source file `hello.c` calls a function `hello_util()`
whose prototype is defined in `hello_util.h` and the implementation is defined
in `hello_util.c`.

There is only one `main()` function in `hello.c`. This project generates only
one executable file. 

## Quick usage

The command below compiles C source files and generate an executable.

    sbt compile

The command below runs the generated executable.

    sbt run
    
You can provide command line arguments to the executable when launching.

    sbt run arg1 arg2

## Description of build.sbt

For those who are new to CxxPlugin, this section briefly explains how build.sbt
can be written for a simple C program project.

### Define a target

CxxPlugin supports multiple targets to generate from one class. Each target
has a type and a name. For example, the line below in `build.sbt` defines
`hello` as a target with `Program` type and the name of "hello".

    val helloExe = Program("hello")

Below is the list of available target types.

 * `Program`: an executable (e.g.`Program("hello")`).
 * `Library`: an archive or a static library. Typically, the file name ends with `.a` on Linux platform (e.g. `Library("hello.a")`).
 * `SharedLibrary`: a shared library. Typically, the file name ends with `.so` on Linux platform (e.g. `SharedLibrary("hello.so")`.

They are defined as case classes and take one `name` argument. The name will
be finally used as the file name of the target.

To tell CxxPlugin to generate an executable "hello", set `ccTargets` setting as
shown below:

    Compile / ccTargets := ListSet(helloExe),

`ccTargets` is defined as `ListSet` which allows you to generate multiple targets.
In this example project, there is only one executable for simplicity.

### Speicfy source files

Now you have to specify, which source files to be compiled and linked. Because
CxxPlugin supports multiple targets, you need to specify which source files
for each target. In this example, there is only one executable target, so the
setting is very simple as shown below: 

    Compile / cSources  := Map(
      helloExe -> Seq(
        baseDirectory.value / "hello.c",
        baseDirectory.value / "hello_util.c"
      )
    ),

This setting basically says `hello.c` and `hello_util.c` need to be compiled
and linked to generate the "hello" executable.
