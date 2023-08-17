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
            "ch.qos.logback" % "logback-classic" % "1.4.7",
            "com.typesafe" % "config" % "1.4.2",
      "com.github.finagle" %% "finchx-core" % finchVersion,
      "com.github.finagle" %% "finchx-circe" % finchVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "com.twitter" %% "finagle-stats" % "22.12.0",
      "io.circe" %% "circe-testing" % "0.14.1" % Test,

      "io.circe" %% "circe-parser" % "0.13.0",
      "com.twitter" %% "finagle-redis" % "22.12.0",
                          mockito,
      scalatest % Test,


    )
  )

