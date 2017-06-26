lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "eu.xeli",
      scalaVersion := "2.12.1",
      version      := "0.3.0"
    )),
    name := "AquariumPI",
    mainClass in (Compile, run) := Some("eu.xeli.aquariumPI.App"),
    mainClass in assembly := Some("eu.xeli.aquariumPI.App"),
    sbtavrohugger.SbtAvrohugger.specificAvroSettings,
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1",
      "com.typesafe" % "config" % "1.3.1",
      "org.scalamock" % "scalamock-scalatest-support_2.12" % "3.6.0",
      "org.apache.avro" % "avro" % "1.8.2",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "eu.xeli" %% "jpigpio" % "0.1.0",
      "org.scalanlp" %% "breeze" % "0.13.1",
      "org.scalanlp" %% "breeze-natives" % "0.13.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "com.github.pureconfig" %% "pureconfig" % "0.7.2"
    ),
    resolvers += "Sonatype OSS Staging" at "https://oss.sonatype.org/content/repositories/staging"
  )

