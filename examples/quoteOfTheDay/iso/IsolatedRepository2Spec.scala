import com.whisk.docker.impl.spotify.DockerKitSpotify
import controllers.QuoteController
import models.Quote
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IsolatedRepository2Spec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with BeforeAndAfterAll
  with DockerMongodbService with DockerKitSpotify
{

  override lazy val HostPort = 27018

  //configure and start dockerized MongoDB for this test suite
  startAllOrFail()
  val app = new GuiceApplicationBuilder()
    .configure(Map("mongodb.uri" -> s"mongodb://mongo:$HostPort/quoteOfTheDay"))
    .build()

  val qc = app.injector.instanceOf[QuoteController]

  "QuoteController repository helpers should" should {

    "find quote by karma" in {
      val newQuote = new Quote(110, "some text 1", "author 1")

      val addAll = Future.sequence{Seq(
        qc.addQuote(newQuote)
      )}

      whenReady(addAll) { result =>
        qc.getQuote(110).futureValue must be (Some(newQuote))
      }
    }

    "find quotes by author" in {
      val q1 = new Quote(160, "some text 6", "author 3")
      val q2 = new Quote(170, "some text 7", "author 3")

      val addAll = Future.sequence{Seq(
        qc.addQuote(q1),
        qc.addQuote(q2)
      )}

      whenReady(addAll) { result =>
        qc.getQuoteByAuthor("author 3").futureValue.size must be (2)
      }
    }
  }

  override def afterAll() = {
    stopAllQuietly()
    super.afterAll()
  }

}
