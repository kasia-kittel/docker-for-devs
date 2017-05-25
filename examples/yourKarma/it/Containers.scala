import com.whisk.docker._


trait Containers extends DockerKit {

  val mongodbPort = 27017
  val mongodbContainerName = "mongo"

  val mongodbContainer = DockerContainer("mongo:3.0.6", Some(mongodbContainerName))
    .withPorts(mongodbPort -> Some(mongodbPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port")) //condition

  val qotdPort = 8081

  //name the container so it will be removed after test finishes
  val qotdContainer = DockerContainer("quoteoftheday:latest", Some("QuoteOfTheDay"))
    .withPorts(qotdPort -> Some(qotdPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("Application started (Prod)"))
    .withCommand("-Dhttp.port=8081",  "-Dplay.crypto.secret=abcdefghijk")
    .withLinks(ContainerLink(mongodbContainer, mongodbContainerName))

  val ylnPort = 8080
  val ylnContainer = DockerContainer("yln:latest", Some("YourLuckyNumber"))
    .withPorts(ylnPort -> Some(ylnPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("Serving on"))

  abstract override def dockerContainers: List[DockerContainer] =
    mongodbContainer :: qotdContainer :: ylnContainer :: super.dockerContainers

}
