name := "olivier-bruchez-name"

version := "2.0.11"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.10.1"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  guice,
  "joda-time" % "joda-time" % "2.15.1",
  "org.apache.commons" % "commons-text" % "1.15.0",
  "org.commonmark" % "commonmark" % "0.32.0",
  "org.commonmark" % "commonmark-ext-gfm-tables" % "0.32.0",
  "org.commonmark" % "commonmark-ext-image-attributes" % "0.32.0",
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
dockerExposedVolumes := Seq(
  "/opt/docker/prod-conf",
  "/var/log/olivier-bruchez-name",
  "/var/cache/olivier-bruchez-name"
)
dockerEnvVars := Map("THUMBNAILS_DIRECTORY" -> "/var/cache/olivier-bruchez-name/thumbnails")
dockerBaseImage := "eclipse-temurin:21-jre-jammy"
dockerBuildOptions ++= Seq("--platform", "linux/amd64")
//dockerBuildOptions ++= Seq("--platform", "linux/arm64")

Docker / daemonUserUid := Some("1099")
Docker / daemonUser := "olivierbruchezname"
Docker / daemonGroupGid := Some("1099")
Docker / daemonGroup := "olivierbruchezname"
