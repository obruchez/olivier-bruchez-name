name := "olivier-bruchez-name"

version := "1.7-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(jdbc, cache, ws)

libraryDependencies ++= Seq(
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev62-1.25.0",
  "com.google.api-client" % "google-api-client" % "1.30.5",
  "com.google.http-client" % "google-http-client" % "1.33.0",
  "com.google.http-client" % "google-http-client-jackson2" % "1.33.0",
  "com.google.oauth-client" % "google-oauth-client" % "1.30.4",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.30.4",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.30.4",
  "joda-time" % "joda-time" % "2.9.9",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "org.jsoup" % "jsoup" % "1.12.1",
  "org.twitter4j" % "twitter4j-core" % "4.0.7"
)

routesGenerator := StaticRoutesGenerator

scalafmtOnCompile in ThisBuild := true

requiredStartFacilities in Debian := Some("$remote_fs $syslog $network")

requiredStopFacilities in Debian := Some("$remote_fs $syslog $network")
