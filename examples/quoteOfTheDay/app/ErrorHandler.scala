import javax.inject.Singleton

import models.Error
import play.api.http.HttpErrorHandler
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)(Json.toJson(Error(request.toString(), message)))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(
      InternalServerError(Json.toJson(Error(request.toString(), exception.getMessage)))
    )
  }
}