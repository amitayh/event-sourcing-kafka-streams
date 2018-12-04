name := "invoices-kafka-streams"
organization in ThisBuild := "org.amitayh"
scalaVersion in ThisBuild := "2.12.3"

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(commonSettings)
  .aggregate(
    common,
    streamprocessor,
    commandhandler,
    listprojector,
    web
  )

lazy val common = project
  .settings(
    name := "common",
    commonSettings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.log4s,
      dependencies.kafkaClients,
      dependencies.avro4s
    )
  )

lazy val listdao = project
  .settings(
    name := "listdao",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.doobie,
      dependencies.doobieHikari
    )
  )
  .dependsOn(common, streamprocessor)

lazy val streamprocessor = project
  .settings(
    name := "streamprocessor",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.kafkaStreams
    )
  )
  .dependsOn(common)

lazy val commandhandler = project
  .settings(
    name := "commandhandler",
    commonSettings,
    assemblySettings,
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(common, streamprocessor)

lazy val listprojector = project
  .settings(
    name := "listprojector",
    commonSettings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.mysqlConnector % Runtime
    )
  )
  .dependsOn(common, streamprocessor, listdao)

lazy val web = project
  .settings(
    name := "web",
    commonSettings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.http4sBlaze,
      dependencies.http4sCirce,
      dependencies.http4sDsl,
      dependencies.circeCore,
      dependencies.circeGeneric,
      dependencies.circeParser,
      dependencies.kafkaClients,
      dependencies.catsRetry,
      dependencies.mysqlConnector % Runtime
    )
  )
  .dependsOn(common, listdao)

// DEPENDENCIES

lazy val dependencies =
  new {
    val logbackVersion          = "1.2.3"
    val slf4jVersion            = "1.7.25"
    val log4sVersion            = "1.6.1"
    val http4sVersion           = "0.18.20"
    val circeVersion            = "0.10.0"
    val doobieVersion           = "0.5.3"
    val kafkaVersion            = "2.0.0"
    val mysqlConnectorVersion   = "8.0.12"
    val avro4sVersion           = "1.9.0"
    val catsRetryVersion        = "0.2.0"
    val origamiVersion          = "5.0.1"
    val producerVersion         = "5.0.0"

    val logback         = "ch.qos.logback"        % "logback-classic"       % logbackVersion
    val slf4j           = "org.slf4j"             % "jcl-over-slf4j"        % slf4jVersion
    val kafkaClients    = "org.apache.kafka"      % "kafka-clients"         % kafkaVersion
    val kafkaStreams    = "org.apache.kafka"      % "kafka-streams"         % kafkaVersion
    val mysqlConnector  = "mysql"                 % "mysql-connector-java"  % mysqlConnectorVersion
    val log4s           = "org.log4s"             %% "log4s"                % log4sVersion
    val http4sBlaze     = "org.http4s"            %% "http4s-blaze-server"  % http4sVersion
    val http4sCirce     = "org.http4s"            %% "http4s-circe"         % http4sVersion
    val http4sDsl       = "org.http4s"            %% "http4s-dsl"           % http4sVersion
    val circeCore       = "io.circe"              %% "circe-core"           % circeVersion
    val circeGeneric    = "io.circe"              %% "circe-generic"        % circeVersion
    val circeParser     = "io.circe"              %% "circe-parser"         % circeVersion
    val doobie          = "org.tpolecat"          %% "doobie-core"          % doobieVersion
    val doobieHikari    = "org.tpolecat"          %% "doobie-hikari"        % doobieVersion
    val avro4s          = "com.sksamuel.avro4s"   %% "avro4s-core"          % avro4sVersion
    val catsRetry       = "com.github.cb372"      %% "cats-retry-core"      % catsRetryVersion
  }

lazy val commonDependencies = Seq(
  dependencies.logback,
  dependencies.slf4j
)

// SETTINGS

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)
