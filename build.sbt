organization := "bruchez.org"

name := "olivier"

version := "0.1"

scalaVersion := "2.9.2"

seq(webSettings :_*)

logLevel := Level.Info

EclipseKeys.withSource := true

transitiveClassifiers := Seq("sources")

resolvers ++= Seq(
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Scala Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scala" at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Media4u101 Repository" at "http://www.media4u101.se:8081/nexus/content/repositories/releases/",
  "Media4u101 SNAPSHOT Repository" at "http://www.media4u101.se:8081/nexus/content/repositories/snapshots/"  
)

libraryDependencies ++= {
  val liftVersion = "2.4" // Put the current/latest lift version here
  Seq(
    "net.liftweb" % "lift-webkit_2.9.1" % liftVersion % "compile->default" withSources(),
    "net.liftweb" % "lift-mapper_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-openid_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-squeryl-record_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-wizard_2.9.1" % liftVersion % "compile->default",
    "net.liftweb" % "lift-testkit_2.9.1" % liftVersion % "compile->default",
    "net.liftmodules" % "lift-jquery-module_2.9.1" % (liftVersion+"-1.0") % "compile->default",
    "net.liftmodules" % "fobo_2.9.1" % (liftVersion+"-0.6.0-SNAPSHOT") withSources()
    )
}

// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.3.v20111011" % "container",
  "com.jolbox" % "bonecp" % "0.7.1.RELEASE" % "compile->default",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "org.slf4j" % "slf4j-log4j12" % "1.6.1" % "compile->default", // Logging
  "junit" % "junit" % "4.8" % "test->default", // For JUnit 4 testing
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "org.specs2" % "specs2_2.9.1" % "1.6.1" % "test"
)
