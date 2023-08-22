import Dependencies._
val finchVersion = "0.33.0"
val circeVersion = "0.12.3"
val scalatestVersion = "3.2.0"
ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.book"
ThisBuild / organizationName := "ing_assessment"

lazy val root = (project in file("."))
  .settings(
    name := "ing_assessment",
    libraryDependencies ++= Seq(
      mockito,
      scalatest % Test,
      logback,
      config,
      finchxCore,
      finchxCirce,
      circeGeneric,
      finagleStats,
      twitterServer,
      circeTesting,
      circeParser,
      finagleRedis
    )
  )
