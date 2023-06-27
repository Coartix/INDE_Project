val scala3Version = "2.13.8"

lazy val root = project
  .in(file("."))
  .settings(
    name := "riot_program",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.apache.kafka" %% "kafka" % "3.4.0",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1"
  )
