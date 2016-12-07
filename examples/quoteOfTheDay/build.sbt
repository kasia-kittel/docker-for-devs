import com.typesafe.sbt.packager.docker.ExecCmd

name := "QuoteOfTheDay"

version := "0.9-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test,
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.0-M10" % Test,
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.0-M10" % Test
)

/**** standard config */
//lazy val root = (project in file(".")).enablePlugins(PlayScala)

/**** test config for docker-compose - this must be enabled to run docker-compose up */
//lazy val DockerComposeTest = config("dct") extend(Test)
//
//lazy val root = (project in file(".")).enablePlugins(PlayScala)
//  .configs(DockerComposeTest)
//  .settings(inConfig(DockerComposeTest)(Defaults.testTasks): _*)
//  .settings(javaOptions in DockerComposeTest += "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay" )

/**** test config for docker-it-scala */
//regular test with dokerized Mongo
//lazy val DockerItScalaTest = config("dis") extend(Test)
//lazy val root = (project in file(".")).enablePlugins(PlayScala)
//  .configs(DockerItScalaTest)
//  .settings(inConfig(DockerItScalaTest)(Defaults.testTasks): _*)
//  .settings(javaOptions in DockerItScalaTest += "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay" )
//  .settings(unmanagedSourceDirectories in Test := Seq(baseDirectory.value  / "dis"))


/**** test config for docker-it-scala demonstrating isolation and parallelism*/
//lazy val DockerItScalaTestNIso = config("niso") extend(Test)
//lazy val DockerItScalaTestIso = config("iso") extend(Test)
//
//lazy val root = (project in file(".")).enablePlugins(PlayScala)
//  .configs(DockerItScalaTestNIso)
//    // fork=true and parallelExecution=true are necessary to run test suites in parallel
//    //.settings(fork in DockerItScalaTestNIso := false)
//    //.settings(parallelExecution in DockerItScalaTestNIso := true)
//    .settings(inConfig(DockerItScalaTestNIso)(Defaults.testTasks): _*)
//    .settings(unmanagedSourceDirectories in Test := Seq(baseDirectory.value  / "iso") )
//    .settings(testOptions in DockerItScalaTestNIso := Seq(Tests.Filter(s => s.startsWith("NotIsolated"))))
//  .configs(DockerItScalaTestIso)
//    .settings(fork in DockerItScalaTestIso := false)
//    .settings(parallelExecution in DockerItScalaTestIso := true)
//    .settings(inConfig(DockerItScalaTestIso)(Defaults.testTasks): _*)
//    .settings(unmanagedSourceDirectories in Test := Seq(baseDirectory.value  / "iso") )
//    .settings(testOptions in DockerItScalaTestIso := Seq(Tests.Filter(s => s.startsWith("Isolated"))))

logBuffered in Test := false


/**** Configure docker deployment **/

/* choose minified base image */
dockerBaseImage := "anapsix/alpine-java:jre"

/* expose 9000 port to make the container contactable */
dockerExposedPorts += 9000

/* add tag latest to the image */
dockerUpdateLatest := true

/**
  *  we may need to install bash as first thing after base image is started
dockerCommands := dockerCommands.value.flatMap{
  case cmd@Cmd("FROM",_) => List(cmd,Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}
  */

/**
  * we need to add some production config
  * for this config a docker container with mongo should run on docker_ip:27017
  * docker run -it --link <mongo-container-name>:mongo <container_id>
  */
dockerCommands := dockerCommands.value.flatMap{
  case cmd@ExecCmd("ENTRYPOINT",command) =>
    List(ExecCmd("ENTRYPOINT",  command, "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay"))
  case other => List(other)
}

/* index.docker.io repo user*/
//dockerRepository := Some("kasiak")

/**
  * now we can run the app: docker run -it --link <mongo-container-name>:mongo -p 8090:9000 quoteoftheday
 */
