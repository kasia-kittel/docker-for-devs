package models

import play.api.libs.json.Json
import play.api.mvc.QueryStringBindable

case class Somebody(name: String, age: Option[Int] = None)

object Somebody {
  implicit val somebodyFormat = Json.format[Somebody]

  implicit def somebodyBindable = new QueryStringBindable[Somebody] {

    val nameKey = "name"
    val ageKey = "age"

    type opStr = QueryStringBindable[String]
    type opInt = QueryStringBindable[Option[Int]]


    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Somebody]] = {
      for {
        name <- implicitly[opStr].bind(nameKey, params)
        age <-  implicitly[opInt].bind(ageKey, params)
      } yield {
        (name, age) match {
          case (Right(name), Right(age)) => Right(Somebody(name, age))
          case (Right(name), _ )=> Right(Somebody(name, None))
          case _ => Left("Unable to bind Somebody")
        }
      }
    }

    override def unbind(key: String, somebody: Somebody): String =
      (implicitly[opStr].unbind(nameKey, somebody.name) + "&" + implicitly[opInt].unbind(ageKey, somebody.age))
        .stripSuffix("&")

  }

}
