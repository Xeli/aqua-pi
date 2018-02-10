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
      "io.cucumber" %% "cucumber-scala" % "2.0.1" % Test,
      "io.cucumber" % "cucumber-junit" % "2.3.1" % Test,
      "io.cucumber" % "gherkin" % "5.0.0" % Test,
      "org.scalamock" %% "scalamock" % "4.0.0" % Test,
      "org.scalactic" %% "scalactic" % "3.0.1" % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,

      "com.typesafe" % "config" % "1.3.1",
      "org.apache.avro" % "avro" % "1.8.2",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "eu.xeli" %% "jpigpio" % "0.1.0",
      "org.scalanlp" %% "breeze" % "1.0-RC2",
      "org.scalanlp" %% "breeze-natives" % "1.0-RC2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "com.github.pureconfig" %% "pureconfig" % "0.8.0"
    ),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
    )
  )

