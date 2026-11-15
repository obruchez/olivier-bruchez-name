name := "olivier-bruchez-name"

version := "2.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.10.1"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  guice,
  "com.github.rjeschke" % "txtmark" % "0.13",
  "com.google.apis" % "google-api-services-blogger" % "v3-rev20260707-2.9.2",
  "com.google.api-client" % "google-api-client" % "2.9.2",
  "com.google.http-client" % "google-http-client" % "2.2.0",
  "com.google.http-client" % "google-http-client-jackson2" % "2.2.0",
  "joda-time" % "joda-time" % "2.15.1",
  "org.apache.commons" % "commons-text" % "1.15.0",
  "org.jsoup" % "jsoup" % "1.24.1",
  "org.scala-lang.modules" %% "scala-xml" % "2.5.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
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
dockerBaseImage := "eclipse-temurin:21-jre-jammy"
dockerBuildOptions ++= Seq("--platform", "linux/amd64")
//dockerBuildOptions ++= Seq("--platform", "linux/arm64")

Docker / daemonUserUid := Some("1099")
Docker / daemonUser := "olivierbruchezname"
Docker / daemonGroupGid := Some("1099")
Docker / daemonGroup := "olivierbruchezname"
