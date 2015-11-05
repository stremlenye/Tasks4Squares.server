package controllers

import javax.inject.Inject

import models.User
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import stores.UsersStore
import entities.Implicits.UserEntityLike

/**
 * Created by yankudinov on 15/09/15.
 */
class UsersController @Inject() (usersStore: UsersStore) extends Controller {

  implicit val userReader: Reads[User] = ((__ \ "id").readNullable[String] and
    (__ \ 'login).read[String] and
    (__ \ 'password).readNullable[String])(User.apply _)

  def create = Action.async(parse.json[User]) {
    request => usersStore.create(request.body) map {
      case 1 => Created
      case _ => InternalServerError
    }
  }
}
