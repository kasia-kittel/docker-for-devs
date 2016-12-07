import controllers.QuoteController
import models.Quote
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class NotIsolatedRepository2Spec extends WordSpec with MustMatchers with OptionValues with ScalaFutures
  with BeforeAndAfterAll {

  //**** Standard test set up for one instance of MongoDB
  //this would use default MongoDB config from application.conf
  //which points to MongoDB running in docker default machine
  //192.168.99.100:27017
  //!! start mongoDB before running test: docker run -p27017:27017 mongo
  val app = new GuiceApplicationBuilder().build()
  val qc = app.injector.instanceOf[QuoteController]

  override def beforeAll() = {
    super.beforeAll()
    Await.result(qc.clearAll(), 5 seconds)
    Await.result(qc.startUp(),  5 seconds)
  }


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

}
