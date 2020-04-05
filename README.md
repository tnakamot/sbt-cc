# sbt CcPlugin (sbt-cc)

This is an sbt plugin to compile C and C++ source files.

## Usage

### Install

This plugin has not been published to any of public maven or ivy2 repositories yet.
To use this plugin, you need to download the source tree, compile and install it
in your local repository. If you have not downloaded the source tree, check it out
with git command.

    $ git clone https://github.com/tnakamot/sbt-cc.git
   
Then, compile it and install with the commands below.

    $ cd sbt-cc
    $ sbt compile publishLocal

The last command `publishLocal` normally installs this plugin into your local
ivy2 repository which is typically located at `~/.ivy2/local`.

### Use this plugin in your sbt project

TODO: explain how to enable this plugin

## Examples

There are several example sbt projects under `example/` directory. README.md in each
example project provides hands-on tutorial about how to configure this plugin for
your C/C++ projects.

Below is the list of the example sbt projects. If you are a beginner, it is recommended
to go through all the examples below in this order:

* [simple-c](examples/simple-c/README.md): the simplest C project
* [multi-execs](examples/multi-execs/README.md): multiple executable targets in one project
* [simple-cxx](examples/simple-cxx/README.md): a simple C++ project

## TODO

* publish this plugin
* make an example of how to add include directories (static and dynamic)
* make an example of how to dynamically add source files
* make an example of static library and shared library
* make an example of how to package the executables and libraries using Universal plugin.
* make an example of mixture of C and C++
* make an example of test
