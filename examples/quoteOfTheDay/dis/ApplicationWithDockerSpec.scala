import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._


class ApplicationWithDockerSpec extends PlaySpec with OneAppPerSuite with PropertyChecks with BeforeAndAfterAll
  with DockerMongodbService with DockerTestKit with DockerKitSpotify {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/booom")).map(status) mustBe Some(NOT_FOUND)
    }
  }

  "QuoteController should " should {

    "return a quote for given Somebody with age" in {
      val response = route(app, FakeRequest(GET, "/quote?name=somebody&age=30")).get

      status(response) must be (OK)
    }

    "return a quote for given Somebody without age" in {
      val response = route(app, FakeRequest(GET, "/quote?name=somebody")).get

      status(response) must be (OK)
    }


    "return error for malformed query string" in {
      forAll { (param: String, value:String) => {
          val response = route(app, FakeRequest(GET, s"/quote?$param=$value&age=-1")).get

          status(response) must be (BAD_REQUEST)
        }
      }

    }
  }


}
