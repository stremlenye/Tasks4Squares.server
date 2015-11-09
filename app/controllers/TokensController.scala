package controllers

import javax.inject.Inject

import filters.{UnauthenticatedRequest, AuthenticatedAction}
import helpers.Id
import models.Token
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import stores.{TokensStore, UsersStore}
import entities.Implicits.{TokenEntityLike, UserEntityLike}
import scala.concurrent.Future

case class LoginForm (login: String, password: String)

class TokensController @Inject() (tokensStore: TokensStore, usersStore: UsersStore) extends Controller {


  implicit val loginFormReader: Reads[LoginForm] = ((__ \ 'login).read[String] and
    (__ \ 'password).read[String])(LoginForm.apply _)

  implicit val tokenWriter: Writes[Token] = ((__ \ "token").write[String] and
    (__ \ "owner").write[String] and
    (__ \ "issuedAd").write[DateTime])(unlift(Token.unapply))

  def create = AuthenticatedAction.async(parse.json[LoginForm]) {
    case r: UnauthenticatedRequest[LoginForm] => usersStore.find(r.request.body.login, r.request.body.password) flatMap {
      case Some(user) => {
        val token = Token(Id.generate, user.id, DateTime.now)
        tokensStore.create(token) map {
          case 1 => Created(Json.toJson(token))
          case 0 => InternalServerError(Json.obj("message" -> "Unable to login"))
        }
      }
      case None => Future(BadRequest(Json.obj("message" -> "No such user")))
    }
    case _ => Future(Unauthorized)
  }
}