addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.7")
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.12")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
