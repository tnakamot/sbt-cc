# sbt CcPlugin (sbt-cc)

This is an sbt plugin to compile C and C++ source files.

## Usage

To use this plugin in your sbt project, add the following lines to `project/plugins.sbt`.

    resolvers += Resolver.bintrayIvyRepo("nyakamoto", "sbt-plugins")
    addSbtPlugin("com.github.tnakamot" % "sbt-cc" % "0.0.3")

If `project/plugins.sbt` does not exist in your project, just make it with the above
two lines. Then, make sure that you import `sbtcc` module in `build.sbt` as shown 
below:

    import sbtcc._

Finally, call `enablePlugins(CcPlugin)` for your project as shown in below. 

    lazy val testProject = (project in file("."))
      .enablePlugins(CcPlugin)
      .settings(
        ... // settings for CcPlugin
       )

All the settings for CcPlugin go inside `.settings( ... )`. See the examples below
to see how you can make settings.

## Examples

There are several example sbt projects under `example/` directory. README.md in each
example project provides hands-on tutorial about how to configure this plugin for
your C/C++ projects.

Below is the list of the example sbt projects. If you are a beginner, it is recommended
to go through all the examples below in this order:

* [simple-c](examples/simple-c/README.md): the simplest C project
* [multi-execs](examples/multi-execs/README.md): multiple executable targets in one project
* [simple-cxx](examples/simple-cxx/README.md): a simple C++ project
* [mix-cxx-and-c](examples/mix-cxx-and-c/README.md): an example to mix C and C++. Also shows how to switch the default commands for compiling, linking and archiving.
* [external-headers](examples/external-headers/README.md): an example to include additional header search paths and source files.
* [packaging-with-universal](examples/packaging-with-universal/README.md): an example to pass the targets of CcPlugin to other tasks and package them.

## For developers

For those who develops and maintians this plugin, there is [developer's note](DEVELOPER.md). It includes the instruction about how to release a new version of this plugin.

## TODO

* make an example of test
