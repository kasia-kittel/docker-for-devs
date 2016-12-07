#QuoteOfTheDay - demo



##Intro

_QuoteOfTheDay_ is a little sample service that returns a smart quote for a given pair of name and age. The purpose of this application to go along with "Docker for dev" tutorials/talks.

##API
1. `/quotes` returns all quotes stored in the microservice

```javascript
[{
  "karma": 1,
  "text": "Whatever the mind of man can conceive and believe, it can achieve.",
  "author": "Napoleon Hill"
}, {
  "karma": 2,
  "text": "You miss 100% of the shots you donâ€™t take.",
  "author": "Wayne Gretzky"
},
... ]
```

2. `/quote?name=somebody` or `/quote?name=somebody&age=30` returns a random quote
```javascript
{
  "karma": 1,
  "text": "Whatever the mind of man can conceive and believe, it can achieve.",
  "author": "Napoleon Hill"
}

```

This service uses MongoDB to store quotes.



## Dockerizing Scala Play application

We can use _sbt-native-packager_ plugin and use `sbt docker:publishLocal` task to generate default image.
Before we try to run the container we should make sure that **MongoDB is running**:

`docker run -p27017:27017 mongo`.

This will start an instance of MongoDB accessible at 192.168.99.100:27017 if default docker machine is used (or at localhost:27017 if native Docker is running)

Theoretically we could run the container now by typing `docker run quoteoftheday:0.9-SNAPSHOT`. The container will start but the microservice will fail at startup. It misses configuration.

Let's have a look at what this task have generated in `/target` directory. There is a _Dockerfile_ in `target/docker`. This file is very basic and may need further customization. We also have a directory with environment prepared to create the image in `target/docker/stage/opt/`.

Let's try to pass missing *_play.crypto.secret_* config to the container:
`docker run quoteoftheday:0.9-SNAPSHOT  -Dplay.crypto.secret=abcdefghijk `

Looks good, the application starts, but we can not access it. We still need to expose application's port and map it to host port:
`docker run --expose=9000 -p:9000:9000 quoteoftheday:0.9-SNAPSHOT -Dplay.crypto.secret=abcdefghijk`

Now everything works just fine. We can assess the application on Docker default machine IP or localhost (port 9000) depending on our local Docker setup.

Since the generated _Dockerfile_ is very basic it may be interesting to explore how we could adjust it to our needs. It would be also useful to reduce the number of _docker run_ parameters. All customization code should be added to _build.sbt_ : (We need to run `sbt docker:publishLocal` after every change to regenerate the _Dockerfile_.)

Firs, lets change _base image_ to something lighter:

```
dockerBaseImage := "anapsix/alpine-java:jre"
```

Then, we can add default exposed ports:
```
dockerExposedPorts += 9000
```

We can also add tag _latest_ to the image:
```
dockerUpdateLatest := true
```

Another thing we could improve is the way we connect to MongoDB. For now we connect via host, but we could also create a link directly to the MongoDB container:

```scala
dockerCommands := dockerCommands.value.flatMap{
  case cmd@ExecCmd("ENTRYPOINT",command) =>
    List(ExecCmd("ENTRYPOINT",  command,
    "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay"))
  case other => List(other)
}
```
 Let's test the application again. First, let's regenerate the _Docker Image_ of our service: 

`sbt docker:publishLocal`

 Then we need to start MongoDB as named container that will facilitate linking to it later: 

`docker run --name mongo_for_qotd mongo`

Finally we can start container with our service: 

`docker run --link mongo_for_qotd:mongo -p:9000:9000 quoteoftheday:0.9-SNAPSHOT -Dplay.crypto.secret=abcdefghijk`

 Application should be up and running.

## Testing with docker-compose.

Our integration tests need an instance of MongoDB. Using _docker-compose_ we can run the tests in isolated environment and be sure all external services (like MongoDB) are ready.

First we would need dockerized _Sbt_, for example [https://github.com/1science/docker-sbt](1science/sbt).

We may also prepare special test config that overwrites _mongo.url_ - for example:

```scala
lazy val DockerComposeTest = config("dct") extend(Test)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .configs(DockerComposeTest)
  .settings(inConfig(DockerComposeTest)(Defaults.testTasks): _*)
  .settings(javaOptions in DockerComposeTest += "-Dmongodb.uri=mongodb://mongo:27017/quoteOfTheDay" )
```
Next we can prepare _docker-compose.yml_ for our tests:

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

Now we can run all tests using `docker-compose up --abort-on-container-exit` (_--abort-on-container-exit_ flag stops docker-compose when one of the containers exits - in our case _sbt_ container when the tests are finish.)

## Testing with docker-it-scala

[Docker-it-scala](https://github.com/whisklabs/docker-it-scala) is a library that can help to dockerize adjacent services and orchestrate them directly from tests.

 With _docker-it-scala_ all we need is prepare a trait with MongoDB container configuration and then mix it in to our test suite along with with `DockerTestKit` (and `DockerKitSpotify` if we prefer to use Spotify Docker API client.)

```scala
trait DockerMongodbService extends DockerKit {

  val DefaultMongodbPort = 27017

  val mongodbContainer = DockerContainer("mongo:3.0.6")
    .withPorts(DefaultMongodbPort -> Some(DefaultMongodbPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port"))
    .withCommand("mongod", "--nojournal", "--smallfiles", "--syncdelay", "0")

  abstract override def dockerContainers: List[DockerContainer] =
    mongodbContainer :: super.dockerContainers
}
```

The advantage of this approach is that all container orchestration happens within source code of our tests.

(To see an example of such tests have a look at `/dis` directory and commented `DockerItScalaTest` test configuration.)

Another benefit is that we can eliminate inconsistency in parallel run tests, for instance while testing repositories. It often happens that tests from different suites interfere. If we run these suites serially we may be fine. But if we want to run them in parallel we may end up with flaky tests.
In this case isolating test environments by having separate databases per test suite may solve the problem. We can easily implement such solution with Docker.
To see example of this implementation check `/iso` directory and `DockerItScalaTestIso` and `DockerItScalaTestIso` test configs.
