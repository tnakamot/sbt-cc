// This file contains the settings to enable sbt-protoc plugin as instructed by
//  https://github.com/thesamet/sbt-protoc
//  (accessed on 2020-04-09)

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.25")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0"
libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.7.0"
