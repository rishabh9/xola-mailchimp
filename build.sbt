name := """xola-mailchimp"""

version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, DebianPlugin)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "uk.co.panaxiom" %% "play-jongo" % "2.0.0-jongo1.3",
  cache,
  javaWs
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

packageDescription := "The XMailchimp Integration for Xola"