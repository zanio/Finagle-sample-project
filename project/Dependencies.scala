import sbt._

object Dependencies {
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.15"

  lazy val mockito = "org.mockito" %% "mockito-scala" % "1.16.46" % Test
}
