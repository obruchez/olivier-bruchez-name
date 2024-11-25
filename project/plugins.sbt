addSbtPlugin("com.beautiful-scala" % "sbt-scalastyle" % "1.5.1")
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.5.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always