import org.scalatestplus.play._

class IntegrationSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory {

 "Application" should {

    "work from within a browser" in {

      go to ("http://localhost:" + port + "/quotes")

      pageSource must include ("{")
    }
  }
}
