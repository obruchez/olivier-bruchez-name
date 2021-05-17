name := "olivier-bruchez-name"

version := "1.8-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(jdbc, cache, ws)

libraryDependencies ++= Seq(
  guice,
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev82-1.25.0",
  "com.google.api-client" % "google-api-client" % "1.31.5",
  "com.google.http-client" % "google-http-client" % "1.39.2",
  "com.google.http-client" % "google-http-client-jackson2" % "1.39.2",
  "com.google.oauth-client" % "google-oauth-client" % "1.31.5",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.31.5",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.31.5",
  "joda-time" % "joda-time" % "2.10.10",
  "org.apache.commons" % "commons-lang3" % "3.12.0",
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.twitter4j" % "twitter4j-core" % "4.0.7"
)

routesGenerator := InjectedRoutesGenerator

scalafmtOnCompile in ThisBuild := true

requiredStartFacilities in Debian := Some("$remote_fs $syslog $network")

requiredStopFacilities in Debian := Some("$remote_fs $syslog $network")
