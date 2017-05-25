#Docker for Scala
* [Buld Docker Images](#build-docker-images)

##Build Docker Images

With _sbt_ we can easily build Docker Images with [http://sbt-native-packager.readthedocs.io/en/v1.1.5/formats/docker.html
](_sbt-native-packager_).

First step is to enable _sbt-native-packager_ in plugins.sbt file:
```addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.5")```

Task `sbt docker:publishLocal` builds Docker Image locally. Besides it also generates _Dockerfile_ in `target/docker` along with full environment prepared to create the image in `target/docker/stage/opt/`. You can use `sbt docker:stage` task if you only need to generate all files.

There is also a lot of options to customize the default _Dockerfile_. We can add them in _build.sbt_.
For example:

1. Change base image
```
dockerBaseImage := "anapsix/alpine-java:jre"
```

2. Add exposed port
```
dockerExposedPorts += 9000
```

3. Add tag latest to the image
```
dockerUpdateLatest := true
```

4. Add command to install bash
```scala
dockerCommands := dockerCommands.value.flatMap{
  case cmd@Cmd("FROM",_) => List(cmd,Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}
```

5. Pass arguments to embeded application
```scala
dockerCommands := dockerCommands.value.flatMap{
  case cmd@ExecCmd("ENTRYPOINT",command) =>
    List(ExecCmd("ENTRYPOINT",  command,
    "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay"))
  case other => List(other)
}
```

We can preview our modification to _Dockerfile_ prior to generate it with `sbt "show dockerCommands"`

If you set up your Docker ID with `dockerRepository := Some("kasiak")` you can even push your images directry to Docker Hub with `sbt docker:publish`. (You need to run `docker login` first.)


If you need more flexibility you can use _sbt-docker_ plugin as addition to _sbt-native-packager_. More details at [https://github.com/marcuslonnberg/sbt-docker](https://github.com/marcuslonnberg/sbt-docker).



###Test setup with Docker-Compose 1science/sbt

While testing microservices there are a lot of scenarios where we need to make sure adjacent services are reachable before test runs. We can make this proceess automatic with docker-compose.
Since it is the Sbt what executes the tests we would need to docerize it and execute the test command in the container.

----
For now before every test we need to make sure that the Mongo DB is running. How we can make the proceess more handy?
There are (at least two possible solutions). Or we integrate Docker to the test, or we run the dockerize the test.
Here is how:

Scenario: I want to run all unit tests (and do not need to start all related services manually)

Solution 1: 1science/sbt is a dockerized sbt image. It will allows us to run tests as a part of docker-compose application.
[https://github.com/1science/docker-sbt](More details about 1science/sbt image)

Example _docker-compose.yml_

```yml
mongo:
    image: mongo
    expose:
        - "27017"
qotd-test:
    image: "1science/sbt:latest"
    command: sbt clean dct:test
    volumes:
        - "$PWD/:/app:rw"
        - "$HOME/.m2:/root/.m2:rw"
        - "$HOME/.ivy2:/root/.ivy2:rw"
    links:
        - mongo
    stdin_open: true
```

We also need to overwrite _mongodb.url_ to the tests. We can prepare test configuration for that purpose.
For example in _build.sbt_:

```scala

lazy val DockerComposeTest = config("dct") extend(Test)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .configs(DockerComposeTest)
  .settings(inConfig(DockerComposeTest)(Defaults.testTasks): _*)
  .settings(javaOptions in DockerComposeTest += "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay" )

```

Now we can run Docker-compose: `docker-compose up --abort-on-container-exit` (_--abort-on-container-exit_ flag stops docker-compose when one of the containers exits - in our case _sbt_ container when the tests are finish.)

---SBT parallel executing tests:
  //ParallelTestExecution parallel execution of test in a suite
  //sbt parallelExecution parallel execution of suites
Play tests are forked by default what causes that they are executed in parallel.
To turn on paraller test exeution forking should be switched off
fork in Test := false,
parallelExecution in Test := true

//sbt "iso:testOnly *FlakyRepositorySpec"

2. docker-it-scala lets create and orchestrate docker container from scala application. We can add it to our tests to





These two solutions have some iportant disadvanteg. Docker container is considered ready once is up. It doesn't mean that the embeded application is operational. For some services it may take a bit of time. If we start the test before the one of the services in not ready it may couse fail negatives in our tests.
(More about it here: https://docs.docker.com/compose/startup-order/)
Docker-it-scala has some aweso feature wich are listeners that can say if empbeded application is ready or not. Unfortunately it lacks some functionality that we can not use in more complex definition... (TODO CHECK IT NOW)


Scenario Flaky tests.

Sbt by default runs test in pararel. Each test is mapped to a task. It can be switched of by `parallelExecution in Test := false`.


---> easy create tests for older/legacy versions of API


-----


-> docker-it-scala
-> sbt-plugin √
-> sbt docker >>>?poco to bylo √
-> 1science/sbt √
-> running pararel tests on one db - causes inpredicted test result.
How to isolate tests running in pararel that normalny would use the same database.
(ScalaTest api to get test name or something....?)

http://sbt-native-packager.readthedocs.io/en/v1.1.5/formats/docker.html
https://github.com/sbt/sbt-native-packager
https://github.com/whisklabs/docker-it-scala


In the end warning about docker beeing slover. It is good practis to make sure we didn't left any dirst behind...
