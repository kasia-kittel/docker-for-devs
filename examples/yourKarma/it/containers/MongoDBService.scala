package containers

import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}

/**
  * Created by kasia on 06/12/2016.
  */
trait MongoDBService extends DockerKit {
  val mongodbPort = 27017
  val mongodbContainerName = "mongoForQotd"

  val mongodbContainer = DockerContainer("mongo:3.0.6", Some(mongodbContainerName))
    .withPorts(mongodbPort -> Some(mongodbPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port"))

  abstract override def dockerContainers: List[DockerContainer] =
    super.dockerContainers :+ mongodbContainer

}
