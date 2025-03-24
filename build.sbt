lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """olivier-bruchez-name""",
    organization := "org.bruchez.olivier",
    version := "2.0.4",
    scalaVersion := "2.13.16",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

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
