package controllers

import helpers.Hashes
import play.api.libs.json.Reads._
import play.api.libs.json.{JsObject, Json, JsPath, Reads}
import play.api.mvc.{Action, Controller}
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// Combinator syntax

// Reactive Mongo imports
import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


/**
 * Created by stremlenye on 13/11/14.
 */
object Users extends Controller with MongoController {

  def users = db.collection[JSONCollection]("users")

  case class RegistrationForm (name: String, login:String, password: String)

  implicit val RegistrationReads : Reads[RegistrationForm]= (
    (JsPath \ "name").read(minLength[String](3)) and
      (JsPath \ "login").read(minLength[String](3)) and
      (JsPath \ "password").read(minLength[String](3))
    )(RegistrationForm.apply _)

  def register = Action.async(parse.json) {
    request => request.body.validate[RegistrationForm](RegistrationReads)
      .fold(invalid => Future{BadRequest},

      userForm => users.find(Json.obj("login" -> userForm.login)).one[JsObject]
        flatMap {
        case u if u.isDefined => Future {Conflict(Json.obj("message" -> "User with same login already exists"))}
        case u if u.isEmpty => {
          val user = Json.obj(
            "_id" -> BSONObjectID.generate.stringify,
            "login" -> userForm.login,
            "name" -> userForm.name,
            "password" -> Hashes.sha(userForm.password))
          users.insert(user) map {
            _ => Created((user - "password") + ("id" -> user \ "_id") - "_id")
          }
        }
      }
    )
  }
}
