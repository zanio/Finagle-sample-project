import Dependencies._
val finchVersion = "0.32.0"
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
            "ch.qos.logback" % "logback-classic" % "1.4.7",
            "com.typesafe" % "config" % "1.4.2",
      "com.github.finagle" %% "finchx-core" % finchVersion,
      "com.github.finagle" %% "finchx-circe" % finchVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      "io.circe" %% "circe-parser" % circeVersion % "test",
//      munit % Test,
//      "ch.qos.logback" % "logback-classic" % "1.4.7",
//      "com.typesafe" % "config" % "1.4.2",
//      "com.github.finagle" %% "finchx-core" % "0.33.0",
//      "com.github.finagle" %% "finchx-circe" % "0.33.0",
////      "io.circe" %% "circe-generic" % "0.14.4",
      "io.circe" %% "circe-parser" % "0.13.0",
      "com.twitter" %% "finagle-redis" % "22.12.0",
//
////      "com.github.finagle" %% "finch-test" % "0.34.1" % Test,
//      scalatest % Test,


    )
  )

