import sbt.Keys._
import sbt._

object Build extends Build {
  val akkaVersion = "2.4.2"

  val defaults = Defaults.coreDefaultSettings ++ List(
    organization := "pl.pholda.pda",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    libraryDependencies ++= List(
      "org.scalactic" %% "scalactic" % "2.2.6",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    )
  )

  lazy val root = project.in(file("./")).settings(defaults).settings(
    name := "core",
    libraryDependencies ++= List(
      "net.ruippeixotog" % "scala-scraper_2.11" % "0.1.2",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "com.github.nscala-time" %% "nscala-time" % "2.10.0"
    )
  )
}