name := "kafka-streams-poc"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "1.0.1",
  "org.apache.kafka" % "kafka-streams" % "1.0.1",

  "io.circe" %% "circe-core" % "0.9.1",
  "io.circe" %% "circe-generic" % "0.9.1",
  "io.circe" %% "circe-parser" % "0.9.1",

  "org.xerial" % "sqlite-jdbc" % "3.21.0.1",
  "com.github.takezoe" %% "scala-jdbc" % "1.0.5"
)
