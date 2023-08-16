import Dependencies._

ThisBuild / scalaVersion     := "2.13.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.book"
ThisBuild / organizationName := "ing_assessment"

lazy val root = (project in file("."))
  .settings(
    name := "ing_assessment",
    libraryDependencies ++= Seq(
      munit % Test,
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.typesafe" % "config" % "1.4.2",
      "com.github.finagle" %% "finch-core" % "0.34.1",
      "com.github.finagle" %% "finch-circe" % "0.34.0",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.1",
      "com.twitter" %% "finagle-redis" % "22.12.0",

//      "com.github.finagle" %% "finch-test" % "0.34.1" % Test,
      scalatest % Test,


    )
  )

