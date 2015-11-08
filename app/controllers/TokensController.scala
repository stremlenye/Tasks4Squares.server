package controllers

import java.util.UUID
import javax.inject.Inject

import models.Token
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import stores.{TokensStore, UsersStore}
import entities.Implicits.{TokenEntityLike, UserEntityLike}
import scala.concurrent.Future

/**
 * Created by yankudinov on 15/09/15.
 */
class TokensController @Inject() (tokensStore: TokensStore, usersStore: UsersStore) extends Controller {

  case class LoginForm (login: String, password: String)

  implicit val userReader: Reads[LoginForm] = ((__ \ 'login).read[String] and
    (__ \ 'password).read[String])(LoginForm.apply _)

  implicit val tokenWriter: Writes[Token] = ((__ \ "token").write[String] and
    (__ \ "owner").write[String] and
    (__ \ "issuedAd").write[DateTime])(unlift(Token.unapply))

  def create = Action.async(parse.json) {
    request => (request.body.validate[LoginForm].fold(
      s => Left("Invalid json"),
      form => Right(form)) match {
      case Right(form) => usersStore.find(form.login, form.password) map {
        case Some(user) => Right(Token(UUID.randomUUID().toString, user.id.get, DateTime.now))
        case None => Left("Login or password is incorrect")
      }
      case Left(m) => Future(Left(m))
    }) flatMap {
      case Right(token) => tokensStore.create(token) map {
        case 1 => Right(token)
        case _ => Left("Unable to create token")
      }
      case Left(m) => Future {
        Left(m)
      }
    } map {
      case Right(token) => Created(Json.toJson(token))
      case Left(m) => InternalServerError(Json.obj("message" -> m)) // TODO: return valid error codes based on real errors
    }
  }
}