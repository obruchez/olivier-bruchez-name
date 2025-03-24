name := "olivier-bruchez-name"

version := "1.9.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(jdbc, ehcache, ws)

libraryDependencies ++= Seq(
  guice,
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev20221220-2.0.0",
  "com.google.api-client" % "google-api-client" % "2.7.2",
  "com.google.http-client" % "google-http-client" % "1.46.3",
  "com.google.http-client" % "google-http-client-jackson2" % "1.46.3",
  "com.google.oauth-client" % "google-oauth-client" % "1.39.0",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.39.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.39.0",
  "joda-time" % "joda-time" % "2.13.1",
  "org.apache.commons" % "commons-text" % "1.13.0",
  "org.jsoup" % "jsoup" % "1.19.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
)

routesGenerator := InjectedRoutesGenerator

ThisBuild / scalafmtOnCompile := true

Debian / requiredStartFacilities := Some("$remote_fs $syslog $network")
Debian / requiredStopFacilities := Some("$remote_fs $syslog $network")

import com.typesafe.sbt.packager.docker.*

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
