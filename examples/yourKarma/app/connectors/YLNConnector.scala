package connectors

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext


class YLNConnector @Inject() (ws: WSClient, val config: Configuration)(implicit context: ExecutionContext) {

  val url = config.underlying.getString("yln.url")

  //number?dateOfBirth=30-Nov-00

  def getLuckyNumberFor(dateOfBirth: String) =
    ws.url(url).withQueryString("dateOfBirth" -> dateOfBirth).get().map {
      r =>
        r.status match {
        case 200 => r.body
        case _ => throw new Exception("wrong response")
      }

    }
}
