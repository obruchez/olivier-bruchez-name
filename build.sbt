name := "olivier-bruchez-name"

version := "1.9"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(jdbc, ehcache, ws)

libraryDependencies ++= Seq(
  guice,
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev82-1.25.0",
  "com.google.api-client" % "google-api-client" % "1.35.2",
  "com.google.http-client" % "google-http-client" % "1.42.3",
  "com.google.http-client" % "google-http-client-jackson2" % "1.42.3",
  "com.google.oauth-client" % "google-oauth-client" % "1.34.1",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.34.1",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.34.1",
  "joda-time" % "joda-time" % "2.12.2",
  "org.apache.commons" % "commons-text" % "1.10.0",
  "org.jsoup" % "jsoup" % "1.15.4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)

routesGenerator := InjectedRoutesGenerator

ThisBuild / scalafmtOnCompile := true

Debian / requiredStartFacilities := Some("$remote_fs $syslog $network")
Debian / requiredStopFacilities := Some("$remote_fs $syslog $network")

import com.typesafe.sbt.packager.docker._

dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerExposedPorts := Seq(9000)
dockerExposedVolumes := Seq("/opt/docker/prod-conf", "/var/log/olivier-bruchez-name")
dockerBaseImage := "openjdk:11-jre-slim-buster"
dockerBuildOptions ++= Seq("--platform", "linux/amd64")
//dockerBuildOptions ++= Seq("--platform", "linux/arm64")

Docker / daemonUserUid := Some("1099")
Docker / daemonUser := "olivierbruchezname"
Docker / daemonGroupGid := Some("1099")
Docker / daemonGroup := "olivierbruchezname"
