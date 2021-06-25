name := "otus-diplom"
version := "0.1"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.12"

lazy val global = project
  .in(file("."))
  .aggregate(
    generator
  )

lazy val domain = project
  .settings(
    name := "domain",
  )

lazy val generator = project
  .settings(
    name := "generator",
    libraryDependencies ++= generatorDependencies
  ).dependsOn(domain)

lazy val batch = project
  .settings(
    assemblyMergeStrategy in assembly := {
      case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
      case x => (assemblyMergeStrategy in assembly).value(x)
    },
    name := "batch",
    libraryDependencies ++= commonDependencies
  ).dependsOn(domain)

lazy val speed = project
  .settings(
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("shapeless.**" -> "shadeshapless.@1").inAll
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
      case x => (assemblyMergeStrategy in assembly).value(x)
    },
    name := "speed",
    libraryDependencies ++= commonDependencies

  ).dependsOn(domain)


lazy val dependencies =
  new {
    val kafkaClientVersion = "2.6.1"
    val logbackVersion = "1.2.3"
    val circeVersion = "0.14.0"
    val scalaCheckVersion = "1.15.4"
    val sparkVersion = "3.0.0"

    val scalaTestVersion = "3.2.1"
    val kafkaClient = "org.apache.kafka" % "kafka-clients" % kafkaClientVersion
    val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
    val circeCore = "io.circe" %% "circe-core" % circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeGenericExtras = "io.circe" %% "circe-generic-extras" % circeVersion
    val circeParser = "io.circe" %% "circe-parser" % circeVersion
    val scalaCheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion
    val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion % Provided
    val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion % Provided
    val sparkStream = "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided
    val sparkSqlKafka = "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion
  }

lazy val commonDependencies = Seq(
  dependencies.kafkaClient,
  dependencies.logback,
  dependencies.circeCore,
  dependencies.circeGeneric,
  dependencies.circeGenericExtras,
  dependencies.circeParser,
  dependencies.scalaCheck,
  dependencies.scalaTest,
  dependencies.sparkCore,
  dependencies.sparkSql,
  dependencies.sparkStream,
  dependencies.sparkSqlKafka
)

lazy val generatorDependencies = Seq(
  dependencies.kafkaClient,
  dependencies.logback,
  dependencies.circeCore,
  dependencies.circeGeneric,
  dependencies.circeGenericExtras,
  dependencies.circeParser,
  dependencies.scalaCheck
)
