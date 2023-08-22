import sbt._

object Dependencies {
  val finchVersion = "0.33.0"
  val circeVersion = "0.12.3"
  val scalatestVersion = "3.2.0"
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.15"
  lazy val mockito = "org.mockito" %% "mockito-scala" % "1.16.46" % Test

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.4.7"
  lazy val config = "com.typesafe" % "config" % "1.4.2"
  lazy val finchxCore = "com.github.finagle" %% "finchx-core" % finchVersion
  lazy val finchxCirce = "com.github.finagle" %% "finchx-circe" % finchVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val finagleStats = "com.twitter" %% "finagle-stats" % "22.12.0"
  lazy val twitterServer = "com.twitter" %% "twitter-server" % "22.12.0"
  lazy val circeTesting = "io.circe" %% "circe-testing" % "0.14.1" % Test
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.13.0"
  lazy val finagleRedis = "com.twitter" %% "finagle-redis" % "22.12.0"

}
