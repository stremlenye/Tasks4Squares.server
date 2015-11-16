package controllers

import connection.{Configuration, Connection}
import filters.{OnFailure, ConnectionApply, UnauthenticatedRequest, AuthenticatedAction}
import helpers.Id
import models.User
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits._
import stores.UsersStore
import entities.Implicits.UserEntityLike

import scala.concurrent.Future

case class SignupForm(login: String, password: String)

class UsersController extends Controller with Connection
  with ConnectionApply with OnFailure {

  override val mongoDbUri: String = Configuration.mongoDbUri()
  override val mongoDefaultDb: String = Configuration.mongoDefaultDb()


  implicit val signupFormReader: Reads[SignupForm] = ((__ \ 'login).read[String] and
    (__ \ 'password).read[String])(SignupForm.apply _)

  def safeUnapply(user: User): Option[(String, String)] = Some(user.id -> user.login)

  implicit val userWriter: Writes[User] = ((__ \ "id").write[String] and
    (__ \ "login").write[String])(unlift(safeUnapply))

  def create = AuthenticatedAction.async(parse.json[SignupForm]) {
    case r: UnauthenticatedRequest[SignupForm] => applyConnection(db => UsersStore(db).find(r.request.body.login) flatMap {
      case None => {
        val user = User(Id.generate, r.request.body.login, Some(r.request.body.password))
        UsersStore(db).create(user) map {
          case 1 => Created(Json.toJson(user))
          case _ => InternalServerError
        }
      }
      case Some(_) => Future(BadRequest(Json.obj("message" -> "User with name given already exists")))
    })
    case _ => Future(Unauthorized)
  }
}
