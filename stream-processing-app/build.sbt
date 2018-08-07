name := "kafka-streams-poc"

version := "0.1"

scalaVersion := "2.12.4"

mainClass in Compile := Some("org.amitayh.invoices.Projector")

val kafkaStreamsVersion = "2.0.0"
val circeVersion = "0.9.3"
val awsSdkVersion = "1.11.381"

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

  "com.amazonaws" % "aws-java-sdk-bom" % awsSdkVersion pomOnly(),
  "com.amazonaws" % "aws-java-sdk-dynamodb" % awsSdkVersion

//  "org.xerial" % "sqlite-jdbc" % "3.21.0.1",
//  "com.github.takezoe" %% "scala-jdbc" % "1.0.5"
)

assemblyMergeStrategy in assembly := {
  case "application.conf" => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
