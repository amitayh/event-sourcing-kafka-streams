name := "kafka-streams-poc"

version := "0.1"

scalaVersion := "2.12.4"

val kafkaStreamsVersion = "2.0.0"
val circeVersion = "0.9.3"

val workaround: Unit = {
  sys.props += "packaging.type" -> "jar"
  ()
}

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaStreamsVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaStreamsVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.xerial" % "sqlite-jdbc" % "3.21.0.1",
  "com.github.takezoe" %% "scala-jdbc" % "1.0.5"
)
