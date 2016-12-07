package models

import play.api.libs.json.Json


case class Error(url: String, message: String)

object Error {
  implicit val errorFormat = Json.format[Error]
}
