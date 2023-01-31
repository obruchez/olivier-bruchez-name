name := "olivier-bruchez-name"

version := "1.8-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(jdbc, ehcache, ws)

libraryDependencies ++= Seq(
  guice,
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev82-1.25.0",
  "com.google.api-client" % "google-api-client" % "2.2.0",
  "com.google.http-client" % "google-http-client" % "1.42.3",
  "com.google.http-client" % "google-http-client-jackson2" % "1.42.3",
  "com.google.oauth-client" % "google-oauth-client" % "1.34.1",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.34.1",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.34.1",
  "joda-time" % "joda-time" % "2.12.2",
  "org.apache.commons" % "commons-text" % "1.10.0",
  "org.jsoup" % "jsoup" % "1.15.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "org.twitter4j" % "twitter4j-core" % "4.0.7"
)

routesGenerator := InjectedRoutesGenerator

ThisBuild / scalafmtOnCompile := true

Debian / requiredStartFacilities := Some("$remote_fs $syslog $network")

Debian / requiredStopFacilities := Some("$remote_fs $syslog $network")
