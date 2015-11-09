package controllers

import javax.inject.Inject

import filters.{UnauthenticatedRequest, AuthenticatedAction}
import helpers.Id
import models.User
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits._
import stores.UsersStore
import entities.Implicits.UserEntityLike

import scala.concurrent.Future

/**
 * Created by yankudinov on 15/09/15.
 */
class UsersController @Inject() (usersStore: UsersStore) extends Controller {

  case class SignupForm(login: String, password: String)

  implicit val signupFormReader: Reads[SignupForm] = ((__ \ 'login).read[String] and
    (__ \ 'password).read[String])(SignupForm.apply _)

  def safeUnapply(user: User): Option[(String, String)] = Some(user.id -> user.login)

  implicit val userWriter: Writes[User] = ((__ \ "id").write[String] and
    (__ \ "login").write[String])(unlift(safeUnapply))

  def create = AuthenticatedAction.async(parse.json[SignupForm]) {
    case r: UnauthenticatedRequest[SignupForm] => usersStore.find(r.request.body.login) flatMap {
      case None => {
        val user = User(Id.generate, r.request.body.login, Some(r.request.body.password))
        usersStore.create(user) map {
          case 1 => Created(Json.toJson(user))
          case _ => InternalServerError
        }
      }
      case Some(_) => Future(BadRequest(Json.obj("message" -> "User with name given already exists")))
    }
    case _ => Future(Unauthorized)
  }
}
