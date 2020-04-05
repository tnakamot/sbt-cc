# simple-cxx

This is an example sbt project to show how to use CcPlugin for a C++ project.
The usage is pretty much is the same as a C project.
The only differences are that the some setting names.
The settings for C++ source files have prefix "cxx" instead of "c".
The mapping between settings for C and C++ is shown below. 

* cCompiler           => cxxCompiler
* cFlags              => cxxFlags
* cSources            => cxxSources
* cSourceFiles        => cxxSourceFiles
* cIncludes           => cxxIncludes
* cIncludeDirectories => cxxIncludeDirectories

