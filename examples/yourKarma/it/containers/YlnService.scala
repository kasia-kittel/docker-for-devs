package containers

import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}

trait YlnService extends DockerKit {

  val ylnPort = 8080
  val ylnContainer = DockerContainer("yln:latest", Some("YourLuckyNumber"))
    .withPorts(ylnPort -> Some(ylnPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("Serving on"))

  abstract override def dockerContainers: List[DockerContainer] =
     super.dockerContainers :+ ylnContainer
}
