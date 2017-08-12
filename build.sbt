name := """olivier-bruchez-name"""

version := "1.5-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws)

libraryDependencies ++= Seq(
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev54-1.22.0",
  "com.google.api-client" % "google-api-client" % "1.22.0",
  "com.google.http-client" % "google-http-client" % "1.22.0",
  "com.google.http-client" % "google-http-client-jackson2" % "1.22.0",
  "com.google.oauth-client" % "google-oauth-client" % "1.22.0",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.22.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0",
  "joda-time" % "joda-time" % "2.9.9",
  "org.apache.commons" % "commons-lang3" % "3.6",
  "org.jsoup" % "jsoup" % "1.10.3",
  "org.twitter4j" % "twitter4j-core" % "4.0.6")

libraryDependencies += guice

routesGenerator := StaticRoutesGenerator
