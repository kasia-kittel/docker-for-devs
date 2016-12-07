import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.selenium.WebBrowser
import org.scalatestplus.play.{PlaySpec, _}

import scala.concurrent.duration._

class IntegrationSpec extends PlaySpec with OneServerPerSuite with WebBrowser with Eventually with BeforeAndAfterAll {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5 seconds)
  implicit val webDriver: WebDriver = new HtmlUnitDriver

  "Application" should {

    "work from within a browser" in {

      go to (s"http://localhost:$port/setup")

      pageTitle mustBe "Your Karma - insert your details"

      textField("name").value = "kasia"
      dateField("dateOfBirth").value = "12-12-1981"
      submit()

      go to (s"http://localhost:$port")
      pageTitle must be ("Your Karma")

    }
  }
}
