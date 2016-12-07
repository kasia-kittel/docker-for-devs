
name := "YourKarma"

version := "0.9-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .settings(unmanagedSourceDirectories in IntegrationTest := Seq(baseDirectory.value  / "it"))

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.0-RC1",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.0-RC1"
)

// needed
fork in Test := false

/* Docker config */
dockerBaseImage := "anapsix/alpine-java:jre"
dockerUpdateLatest := true