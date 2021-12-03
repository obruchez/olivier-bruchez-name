lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """olivier-bruchez-name""",
    organization := "org.bruchez.olivier",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.7",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
