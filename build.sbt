lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """olivier-bruchez-name""",
    organization := "org.bruchez.olivier",
    version := "    version := "2.0.",
    scalaVersion := "2.13.15",
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
