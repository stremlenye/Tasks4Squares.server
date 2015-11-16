package controllers

import connection.{Configuration, Connection}
import filters.{OnFailure, ConnectionApply, UnauthenticatedRequest, AuthenticatedAction}
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

class TokensController extends Controller with Connection with ConnectionApply with OnFailure {

  override val mongoDbUri: String = Configuration.mongoDbUri()
  override val mongoDefaultDb: String = Configuration.mongoDefaultDb()

  implicit val loginFormReader: Reads[LoginForm] = ((__ \ 'login).read[String] and
    (__ \ 'password).read[String])(LoginForm.apply _)

  implicit val tokenWriter: Writes[Token] = ((__ \ "token").write[String] and
    (__ \ "owner").write[String] and
    (__ \ "issuedAd").write[DateTime])(unlift(Token.unapply))

  def create = AuthenticatedAction.async(parse.json[LoginForm]) {
    case r: UnauthenticatedRequest[LoginForm] => applyConnection(db => UsersStore(db)
      .find(r.request.body.login, r.request.body.password) flatMap {
      case Some(user) => {
        val token = Token(Id.generate, user.id, DateTime.now)
        TokensStore(db).create(token) map {
          case 1 => Created(Json.toJson(token))
          case 0 => InternalServerError(Json.obj("message" -> "Unable to login"))
        }
      }
      case None => Future(BadRequest(Json.obj("message" -> "No such user")))
    })
    case _ => Future(Unauthorized)
  }
}