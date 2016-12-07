package controllers

import com.google.inject.Inject
import connectors.{QOTDConnector, YLNConnector}
import org.joda.time.LocalDate
import play.api.Configuration
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, Controller, Cookie, _}

import scala.concurrent.Future
import scala.language.postfixOps

case class UserData(name: String, dateOfBirth: LocalDate){
  def toCookie = s"$name^$dateOfBirth"
}

object UserData {
  implicit val userDataFormater = Json.format[UserData]
}

class YourCarmaController @Inject()(val ylnConnector: YLNConnector, val qotdConnector: QOTDConnector, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller  with I18nSupport with ControllerUtils {
  val cookieName = config.underlying.getString("cookie.name")

  val userForm: Form[UserData] = Form(
    mapping(
      "name" -> nonEmptyText,
      "dateOfBirth" -> jodaLocalDate("dd-MM-yyyy")
    )(UserData.apply)(UserData.unapply)
  )

  def index = Action.async {
    implicit request => { withCookie[UserData] {
      userData =>
        def getData() =
          for {
            ln <- ylnConnector.getLuckyNumberFor(convertDateForYLN(userData.dateOfBirth))
            quote <- qotdConnector.getQuoteFor(userData.name, calculateAge(userData.dateOfBirth))
          } yield {
            Ok(views.html.karma(ln, quote))
          }

        getData().recover {
          case e: Exception => Ok(views.html.error("something bad has happened"))
        }
    }
    }
  }

  def showForm = Action {
    Ok(views.html.form(userForm)).discardingCookies(DiscardingCookie(cookieName))
  }

  def setup = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.form(formWithErrors))
      },
      userData => {
        Redirect(routes.YourCarmaController.index())
          .withCookies(Cookie(cookieName, encode(Json.toJson(userData))))
      }
    )
  }

  def withCookie[T](f: (T) => Future[Result])(implicit request: Request[AnyContent], reads: Reads[T]) =
    request.cookies.get(cookieName) match {
      case Some(cookie) => {
        Json.parse(decode(cookie.value)).asOpt[T].fold(
          Future.successful(BadRequest(s"don't touch my cookies!")))(f(_))
      }
      case None => Future.successful(Redirect(routes.YourCarmaController.showForm()))
    }

}
