import com.whisk.docker.impl.spotify.DockerKitSpotify
import controllers.QuoteController
import models.Quote
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IsolatedRepository1Spec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with BeforeAndAfterAll
  with DockerMongodbService with DockerKitSpotify
{

  override lazy val HostPort = 27019

  //configure and start dockerized MongoDB
  startAllOrFail()
  val app = new GuiceApplicationBuilder()
    .configure(Map("mongodb.uri" -> s"mongodb://mongo:$HostPort/quoteOfTheDay"))
    .build()


  val qc = app.injector.instanceOf[QuoteController]

  "QuoteController repository helpers should" should {

    "do start app with 10 first quotes" in {
      qc.countQuotes().futureValue must be (10)
    }

    "add more 3 Quotes" in {
      val q1 = new Quote(11, "some text 1", "author 1")
      val q2 = new Quote(12, "some text 2", "author 2")
      val q3 = new Quote(13, "some text 3", "author 3")

      val addAll = Future.sequence{Seq(
        qc.addQuote(q1),
        qc.addQuote(q2),
        qc.addQuote(q3)
      )}

      whenReady(addAll) { result =>
        qc.countQuotes().futureValue must be (13)
      }
    }

    "add two more Quotes" in {
      val q1 = new Quote(14, "some text 4", "author 2")
      val q2 = new Quote(15, "some text 5", "author 1")

      val addAll = Future.sequence{Seq(
        qc.addQuote(q1),
        qc.addQuote(q2)
      )}

      whenReady(addAll) { result =>
        qc.countQuotes().futureValue must be (15)
      }
    }
  }

  override def afterAll() = {
    stopAllQuietly()
    super.afterAll()
  }
}
