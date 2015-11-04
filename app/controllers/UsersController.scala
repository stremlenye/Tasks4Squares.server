package controllers

import models.User
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

/**
 * Created by yankudinov on 15/09/15.
 */
object UsersController extends Controller {

  implicit val userReader: Reads[User] = ((__ \ "id").readNullable[String] and
    (__ \ 'login).read[String] and
    (__ \ 'password).readNullable[String])(User.apply _)

  def create = Action.async(parse.json[User]){
    request => {
      Future {
        Ok
      }
    }
  }
}
