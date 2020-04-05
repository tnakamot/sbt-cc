# multi-execs

This example sbt project shows 

* How multiple executables can be generated and executed with CcPlugin.
* How to set compiler flags in CcPlugin.

## Multiple executable outputs

As can be seen in build.sbt, this project defines three executable outputs:

    val programA = Executable("programA")
    val programB = Executable("programB")
    val programC = Executable("programC")
 
    ...
    
    Compile / ccTargets := ListSet(programA, programB, programC),
     
The source files of each executable are specified in the following lines.
In order to compile source files and generate all three executables, run

    $ sbt compile
    
If you want to generate only a specific executable, run

    $ sbt ccLink programA
    
You can specify multiple executables like

    $ sbt ccLink programB programC

## Run an executable 

If you execute `run` command, you will find `programB` is executed.

    $ sbt run
    
This is because the default executable for `run` command is defined with 
`ccRunExecutable` setting as shown below:    
    
    Compile / ccRunExecutable := Some(programB),

Please comment out the above line in `build.sbt` and run this command again.

    $ sbt run
    
Then, `programA` is executed this time. This is because CcPlugin picks up the
first executable in `ccTargets` if `ccRunExecutable` is not set.

If you want to run an executable without changing `build.sbt`, use
`runExecutable` task as below

    $ sbt runExecutable programC
    
You can also add command line arguments.

    $ sbt runExecutable programC hello
    
## Compiler flags

There are two types of flags; one for compiler and the other for linker.
To set the compiler flags, use `cFlags` for C source files and `cxxFlags`
for C++ source files.

The compiler flags are defined in three levels; per-project, per-target and
per-source. The code block shown below is the per-project setting. 

    Compile / cFlags := (Compile / cFlags).value.withDefaultValue( Seq(
      "-Wall", "-Werror",
      "-DPROGRAM_NAME=\"" + name.value + "\"",
      "-DVERSION=\"" + version.value +"\"",
    )),

If there is no per-target or per-source file flags, the per-project setting
is applied when compiling the C source files. For example, the executable
target `programA` in `build.sbt` does not have any per-target or per-source
settings. So, the above flags are used to compile the C source files for
`programA`.

The code block below shows an example of a per-target setting.

    Compile / cFlags ++= Map(
      programB -> Seq( "-DPROGRAM_NAME=\"Program B\"", "-DVERSION=\"0.99.0\"" ),
    ),

This setting is applied to all C source files for `programB`. Note that the 
entire per-project flags are overwritten by the per-target setting. The
per-target setting does not add to the per-project flags. Therefore,
you have to specify all required flags in the per-target setting.

The third line in the code block below shows an example of a per-source setting.

    Compile / cFlags ++= Map(
      programC -> Seq( "-DPROGRAM_NAME=\"Program C\"", "-DVERSION=\"0.0.1\"" ),
      (programC and (baseDirectory.value / "program_c" / "util_aux.c")) -> Seq( "-DPROGRAM_NAME=\"Program C Premium\"", "-DVERSION=\"0.0.1\"" ),
    ),

The third line indicates that the compiler uses `-DPROGRAM_NAME="Program C Premium"`
and `-DVERSION=0.0.1` flags when compiling `program_c/util_aux.c` for `programC`.
Although there is a per-target setting in the second line, the per-source setting
takes precedence. Therefore, the per-source setting is applied to `program_c/util_aux.c`
while the per-target setting is applied to other source files for `programC`.
 
Again, the per-source setting does not add to the per-target or the per-project
settings. You have to specify all required flags in the per-source setting if you
choose to you a per-source setting.

## Linker flags

`ldFlags` is the flags for the linker. The linker has two levels; per-project
and per-target. Unlike the compiler flags, there is no per-source setting, but
the usage of the other two is quite the same as compiler flags.
 
The code line below shows an example of a per-project setting. All targets
which do not have a per-target setting use this setting.
 
    Compile / ldFlags := (Compile / ldFlags).value.withDefaultValue( Seq( "-lm" ) ),

The code line below shows an example of a per-target setting. This setting
applies to only `programB`.

    Compile / ldFlags ++= Map( programB -> Seq("-lm", "-lpthread") ),



