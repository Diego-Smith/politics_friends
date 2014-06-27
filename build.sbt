import sbt._
import sbt.Keys._
import play.Project._

name := "politics-friends"

version := "1.0-SNAPSHOT"



//lazy val root = (project in file(".")).addPlugins(SbtWeb)
scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.1.0",
  "com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "jquery" % "1.8.3"
)

play.Project.playScalaSettings

scalaVersion := "2.10.4"

autoScalaLibrary := false