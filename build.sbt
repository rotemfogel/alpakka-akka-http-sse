name := "akka-sse"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Xfuture",
  "-Xlint",
  "-Yrangepos",
  "-Ywarn-dead-code",
  "-Ywarn-nullary-unit",
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
scalacOptions in Test ++= Seq("-Yrangepos")

compileOrder := CompileOrder.JavaThenScala

resolvers ++= Seq(
  "maven" at "https://maven-central.storage-download.googleapis.com/repos/central/data/",
  "confluent" at "https://packages.confluent.io/maven/"
)

mainClass in (Compile, packageBin) := Some("me.rotemfo.violations.Main")

libraryDependencies ++= {
  lazy val akkaVersion      = "2.5.23"
  lazy val akkaHttpVersion  = "10.1.9"
  lazy val circeVersion     = "0.11.1"
  lazy val confluentVersion = "5.2.1"

  Seq(
    "org.slf4j"          % "slf4j-api"                % "1.7.25",
    "ch.qos.logback"     % "logback-classic"          % "1.2.3",
    "org.apache.kafka"   % "kafka-clients"            % "2.2.0",
    "io.confluent"       % "kafka-avro-serializer"    % confluentVersion excludeAll(excludeApacheLogging, excludeSlf4j),
    "org.json4s"        %% "json4s-native"            % "3.6.5",
    "com.typesafe.akka" %% "akka-slf4j"               % akkaVersion,
    "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream-kafka"        % "1.0.5",
    "de.heikoseeberger" %% "akka-http-circe"          % "1.27.0",
    "io.circe"          %% "circe-generic"            % circeVersion,
    "io.circe"          %% "circe-java8"              % circeVersion,

    "org.specs2"        %% "specs2-core"              % "4.6.0"          % Test
  )
}

val excludeApacheLogging = ExclusionRule(organization = "org.apache.logging.log4j")
val excludeSlf4j = ExclusionRule(organization = "org.slf4j")

(stringType in AvroConfig) := "String"
unmanagedBase in Compile := sourceManaged.value
