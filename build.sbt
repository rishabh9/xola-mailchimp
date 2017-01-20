import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

name := """xola-mailchimp"""

version := (version in ThisBuild).value

lazy val root = (project in file(".")).enablePlugins(PlayJava, DebianPlugin)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  "uk.co.panaxiom" %% "play-jongo" % "2.0.0-jongo1.3",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "org.powermock" % "powermock-module-junit4" % "1.6.6" % "test",
  "org.powermock" % "powermock-api-mockito" % "1.6.6" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.3.1" % "test"
)

PlayKeys.externalizeResources := false

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

libraryDependencies += filters

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

maintainer in Linux := "Rishabh Joshi <rishabh@xola.com>"

packageSummary in Linux := "The Mailchimp Integration for Xola"

packageDescription := "The Mailchimp Integration for Xola"

javaOptions in Test += "-Dconfig.file=conf/application-test.conf"

javaOptions in Test += "-Dlogger.file=conf/logback-test.xml"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8")

testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-v"))

// Uncomment below if play doesn't auto reload in vagrant
// PlayKeys.playWatchService := play.sbtplugin.run.PlayWatchService.sbt(pollInterval.value)

// The Release configuration
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,              // : ReleaseStep
  inquireVersions,                        // : ReleaseStep
  runTest,                                // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  //publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion,                         // : ReleaseStep
  commitNextVersion,                      // : ReleaseStep
  pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
)
