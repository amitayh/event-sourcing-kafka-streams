name := "kafka-streams-poc"

version := "0.1"

scalaVersion := "2.12.4"

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.18")

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "1.0.1",
  "org.apache.kafka" % "kafka-streams" % "1.0.1",

  "io.circe" %% "circe-core" % "0.9.1",
  "io.circe" %% "circe-generic" % "0.9.1",
  "io.circe" %% "circe-parser" % "0.9.1",

  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.github.takezoe" %% "scala-jdbc" % "1.0.5"
)
