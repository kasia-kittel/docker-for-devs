package containers

import com.whisk.docker.{ContainerLink, DockerContainer, DockerKit, DockerReadyChecker}

trait QotdService extends DockerKit {
  this: MongoDBService =>

  val qotdPort = 8081

  //name the container so it will be removed after test finishes
  val qotdContainer = DockerContainer("quoteoftheday:latest", Some("QuoteOfTheDay"))
    .withPorts(qotdPort -> Some(qotdPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("Application started (Prod)"))
    .withCommand("-Dhttp.port=8081",  "-Dplay.crypto.secret=abcdefghijk")
    .withLinks(ContainerLink(mongodbContainer, mongodbContainerName))

  abstract override def dockerContainers: List[DockerContainer] =
     super.dockerContainers :+ qotdContainer

}
