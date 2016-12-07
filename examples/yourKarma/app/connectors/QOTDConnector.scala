package connectors

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

case class Quote(text: String, author: String)
object Quote {
  implicit val quoteFormatter = Json.format[Quote]
}

class QOTDConnector @Inject() (ws: WSClient, val config: Configuration)(implicit context: ExecutionContext){

  val nameKey = "name"
  val ageKey = "age"

  val url = config.underlying.getString("qotd.url")

  def getQuoteFor(name: String, age: Int) =
    ws.url(url).withQueryString(nameKey -> name, ageKey -> age.toString).get().map {
      r =>
        r.status match {
          case 200 => Json.parse(r.body).asOpt[Quote] match {
            //TODO - make it better
            case Some(quote) => quote
            case None => throw new Exception("wrong response")
          }
          case _ => throw new Exception("wrong response")
        }
    }
}
