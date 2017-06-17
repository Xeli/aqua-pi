import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "eu.xeli",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "AquariumPI",
    mainClass in (Compile, run) := Some("eu.xeli.aquariumPI.App"),
    sbtavrohugger.SbtAvrohugger.specificAvroSettings,
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "com.typesafe" % "config" % "1.3.1",
      "org.apache.avro" % "avro" % "1.8.2",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "eu.xeli" %% "jpigpio" % "0.1.0",
      "org.scalanlp" %% "breeze" % "0.13.1",
      "com.github.pureconfig" %% "pureconfig" % "0.7.2"
    ),
    resolvers += "Sonatype OSS Staging" at "https://oss.sonatype.org/content/repositories/staging"
  )