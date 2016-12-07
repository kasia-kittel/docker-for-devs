import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}


trait DockerMongodbService extends DockerKit {

  val DefaultMongodbPort = 27017
  lazy val HostPort = 0

  val mongodbContainer = DockerContainer("mongo:3.0.6")
    .withPorts(DefaultMongodbPort -> Some(HostPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port"))
    .withCommand("mongod", "--nojournal", "--smallfiles", "--syncdelay", "0")

  abstract override def dockerContainers: List[DockerContainer] =
    mongodbContainer :: super.dockerContainers

}