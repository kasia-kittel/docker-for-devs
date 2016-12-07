import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import containers.{MongoDBService, QotdService, YlnService}
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.selenium.WebBrowser
import org.scalatestplus.play.{PlaySpec, _}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration._

class SmokeSpec extends PlaySpec with OneServerPerSuite with WebBrowser with Eventually with BeforeAndAfterAll
  with Containers with DockerTestKit with DockerKitSpotify {
  //with MongoDBService with YlnService with QotdService with DockerTestKit with DockerKitSpotify {

  implicit override lazy val app = new GuiceApplicationBuilder()
    .configure(Map("yln.url" -> s"http://192.168.99.100:$ylnPort/number", "qotd.url" -> s"http://192.168.99.100:$qotdPort/quote")).build()


  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5 seconds)
  implicit val webDriver: WebDriver = new HtmlUnitDriver

  "Application" should {

    "work from within a browser" in {

      go to s"http://localhost:$port/setup"

      pageTitle mustBe "Your Karma - insert your details"

      textField("name").value = "kasia"
      dateField("dateOfBirth").value = "12-12-1981"
      submit()

      go to s"http://localhost:$port"
      pageTitle must be ("Your Karma")

    }
  }
}
