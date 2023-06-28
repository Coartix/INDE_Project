val scala3Version = "2.13.8"

lazy val root = project
  .in(file("."))
  .settings(
    name := "riot_program",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.apache.kafka" %% "kafka" % "3.4.0",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1",
    libraryDependencies += "software.amazon.awssdk" % "s3" % "2.17.12",

    libraryDependencies ++= {
    val sparkVersion = "3.4.0" // Vérifiez que vous utilisez la bonne version de Spark ici
    Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "org.apache.spark" %% "spark-streaming" % sparkVersion,
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion, // pour Kafka
    )
    },
    libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.3.4",
    libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.3.4"
  )
