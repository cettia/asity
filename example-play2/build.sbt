lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "io.cettia.asity" % "asity-bridge-play2" % "3.0.0-Beta1-SNAPSHOT"
libraryDependencies += "io.cettia.asity" % "asity-example" % "3.0.0-Beta1-SNAPSHOT"

resolvers += Resolver.mavenLocal
