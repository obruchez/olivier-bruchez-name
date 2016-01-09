name := """olivier-bruchez-name"""

version := "1.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws)

libraryDependencies ++= Seq(
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev47-1.20.0",
  "com.google.api-client" % "google-api-client" % "1.20.0",
  "com.google.http-client" % "google-http-client" % "1.20.0",
  "com.google.http-client" % "google-http-client-jackson2" % "1.20.0",
  "com.google.oauth-client" % "google-oauth-client" % "1.20.0",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.20.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.20.0",
  "joda-time" % "joda-time" % "2.7",
  "org.twitter4j" % "twitter4j-core" % "4.0.3")

