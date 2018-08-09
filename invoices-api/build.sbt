val Http4sVersion = "0.18.14"
val LogbackVersion = "1.2.3"
val KafkaStreamsVersion = "2.0.0"
val AwsSdkVersion = "1.11.381"

lazy val root = (project in file("."))
  .settings(
    organization := "org.amitayh",
    name := "invoices-api",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "org.http4s"        %% "http4s-blaze-server"  % Http4sVersion,
      "org.http4s"        %% "http4s-circe"         % Http4sVersion,
      "org.http4s"        %% "http4s-dsl"           % Http4sVersion,
      "org.apache.kafka"  % "kafka-clients"         % KafkaStreamsVersion,
      "com.amazonaws"     % "aws-java-sdk-bom"      % AwsSdkVersion pomOnly(),
      "com.amazonaws"     % "aws-java-sdk-dynamodb" % AwsSdkVersion,
      "ch.qos.logback"    %  "logback-classic"      % LogbackVersion
    )
  )

