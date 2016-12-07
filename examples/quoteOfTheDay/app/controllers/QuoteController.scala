package controllers
import com.google.inject.Inject
import models.{Quote, Somebody}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Random

class QuoteController @Inject() ( val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents with Utils {

  Await.result(startUp(), 10 seconds)

  /* creates db and collection if they do not exist before first call */
  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("quotes"))

  def getQuoteForSomebody(somebody: Somebody) = Action.async {
    implicit request => {
      collection.flatMap(_.find(Json.obj("karma" -> JsNumber(calculateKarma(somebody)))).one[Quote]) map {
        case Some(q) => Ok(Json.toJson(q))
        case _ => NotFound("No more quotes")
      }
    }
  }

  def getQuotes = Action.async {
    val futureQuotesList: Future[List[Quote]] = collection.flatMap {
      _.find(Json.obj()).cursor[Quote]().collect[List]()
    }

    futureQuotesList.map {
      quotes => Ok(Json.toJson(quotes))
    }
  }

  def startUp() = {

    Logger.info("Preparing database...")

    /* Create indexes */
    Await.result(collection.flatMap(_.indexesManager.ensure(Index(Seq("karma" -> IndexType.Ascending), unique = true))), 5 seconds)

    val loaded =
      collection.flatMap(_.bulkInsert(ordered = false)(
        BSONDocument("karma" -> 1, "text" -> "Whatever the mind of man can conceive and believe, it can achieve.", "author" -> "Napoleon Hill"),
        BSONDocument("karma" -> 2, "text" -> "You miss 100% of the shots you don’t take.", "author" -> "Wayne Gretzky"),
        BSONDocument("karma" -> 3, "text" -> "The most difficult thing is the decision to act, the rest is merely tenacity.", "author" -> "Amelia Earhart"),
        BSONDocument("karma" -> 4, "text" -> "When nothing is sure, everything is possible.", "author" -> "Margaret Drabble"),
        BSONDocument("karma" -> 5, "text" -> "We can't help everyone, but everyone can help someone.", "author" -> "Ronald Reagan"),
        BSONDocument("karma" -> 6, "text" -> "Don't count the days. Make the days count.", "author" -> "Muhammad Ali"),
        BSONDocument("karma" -> 7, "text" -> "Always do your best. What you plant now, you will harvest later.",  "author" -> "Og Mandino"),
        BSONDocument("karma" -> 8, "text" -> "Without a customer, you don’t have a business -- all you have is a hobby..",  "author" -> "Don Peppers"),
        BSONDocument("karma" -> 9,
          "text" ->
            s"""Twenty years from now you will be more disappointed by
               |the things that you didn't do than by the ones you did do.
               |So throw off the bowlines. Sail away from the safe harbor.
               |Catch the trade winds in your sails. Explore. Dream. Discover.""".stripMargin,
          "author" -> "Mark Twain"),
         BSONDocument("karma" -> 10, "" +
           "text" ->
             """Happiness is an attitude. We either make ourselves miserable,
               |or happy and strong. The amount of work is the same""".stripMargin,
           "author" -> "Carlos Castaneda")
      )).map(r => r.ok)

    loaded.onFailure {
      case exception:Exception => throw new RuntimeException("Can not connect to the db")
    }

    loaded.onSuccess {
      case ok => if(!ok) throw new RuntimeException("Data not loaded")
    }

    loaded
  }

  //repository style functions
  def addQuote(quote: Quote) = collection.flatMap(_.insert(quote)).map(wr => wr.ok)
  def countQuotes() = collection.flatMap(_.count())
  def getQuote(karma: Int) = collection.flatMap(_.find(Json.obj("karma" -> JsNumber(karma))).one[Quote])
  def getQuoteByAuthor(author: String) = collection.flatMap(_.find(Json.obj("author" -> JsString(author))).cursor[Quote]().collect[List]())
  def clearAll() = collection.flatMap(_.remove(Json.obj())).map(wr => wr.ok)

}

trait Utils {
  def calculateKarma(somebody: Somebody): Int = {
    val timestamp = DateTime.now.getMillisOfDay
    val ran = Random.nextInt(99)+1
    val karma = Math.abs(somebody.age.getOrElse(1) + timestamp * (somebody.name.length + ran)).toString.head.asDigit
    karma
  }

}

object Utils extends Utils