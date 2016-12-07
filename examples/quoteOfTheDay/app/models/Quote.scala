package models

import play.api.libs.json.Json

case class Quote(karma:Int, text: String, author: String)

object Quote {
  implicit val quoteFormat = Json.format[Quote]
}
