organization := "bruchez.org"

name := "olivier"

version := "0.1"

scalaVersion := "2.10.1"

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
  val liftVersion = "2.5-RC5"
  Seq(
    "net.liftweb" % "lift-webkit_2.10" % liftVersion % "compile->default" withSources(),
    "net.liftweb" % "lift-mapper_2.10" % liftVersion % "compile->default",
    "net.liftweb" % "lift-squeryl-record_2.10" % liftVersion % "compile->default",
    "net.liftweb" % "lift-testkit_2.10" % liftVersion % "compile->default",
    "net.liftweb" % "lift-wizard_2.10" % liftVersion % "compile->default",
    "net.liftmodules" % "lift-jquery-module_2.10" % "2.5-RC4-2.3" % "compile->default",
    "net.liftmodules" % "openid_2.10" % "2.5-RC4-1.2" % "compile->default"
  )
}

// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.3.170",
  "com.jolbox" % "bonecp" % "0.7.1.RELEASE" % "compile->default",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "junit" % "junit" % "4.8" % "test->default",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.3.v20111011" % "container",
  "org.slf4j" % "slf4j-log4j12" % "1.6.1" % "compile->default",
  "org.specs2" % "specs2_2.10" % "1.14" % "test"
)
