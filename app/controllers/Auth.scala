package controllers

import helpers.Hashes
import models.User
import play.api.Logger
import play.api.libs.json._
import reactivemongo.core.commands.LastError

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax
import play.api.mvc._

// Reactive Mongo imports
import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Created by stremlenye on 17/10/14.
 * Authentication controller
 */
object Auth extends Controller with MongoController {

  def users = db.collection[JSONCollection]("users")

  case class LoginForm (login:String, password: String)

  implicit val loginFormat : Reads[LoginForm]= (
    (JsPath \ "login").read(minLength[String](3)) and
    (JsPath \ "password").read(minLength[String](3))
  )(LoginForm.apply _)


  def signIn = Action.async(parse.json) {
    request => {
      request.body.validate[LoginForm].fold(invalid => Future {
        BadRequest("Bad request")
      },
      loginForm => {
        users.find(
          Json.obj("login" -> loginForm.login,
            "password" -> Hashes.sha(loginForm.password))
        ).one[JsObject] flatMap {
          case None => Future {
            Unauthorized(Json.obj("message" -> "Login or password is incorrect"))
          }
          case Some(user) => {
            val token = java.util.UUID.randomUUID.toString
            users.update(user, Json.obj("$set" -> Json.obj("token" -> token))) map {
              _ => Ok(Json.obj("token" -> token, "userId" -> (user \ "_id")))
            } recover {
              case throwable => {
                Logger.error("Token creation failed", throwable)
                InternalServerError(Json.obj("message" -> "Token creation failed"))
              }
            }
          }
        }
      })
    }
  }


  def signOut = Authenticated {
    request => Ok.withCookies(DiscardingCookie("token") toCookie)
  }

  implicit val taskItemFormat: Reads[User] = ((__ \ "_id").read[String] and
                                              (__ \ "name").read[String] and
                                              (__ \ "login").read[String])(User.apply _)

  class AuthenticatedRequest[A](val token: String, request: Request[A], val user: User) extends WrappedRequest[A](request)

  object Authenticated extends ActionBuilder[AuthenticatedRequest]{
    override def invokeBlock[A](request: Request[A],
                                block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      (request.headers.get("Authorization") match {
        case None => None
        case Some(header) => Some(header.replaceFirst("Token", "").trim)
      }) map {
        token => {
          users.find(Json.obj("token" -> token)).one[User] flatMap {
            u => u match {
              case None => Future{Unauthorized(Json.obj("message" -> "session expired"))}
              case Some(user) => block(new AuthenticatedRequest(token, request, user))
            }
          }
        }
      } getOrElse {
        Future{
          Unauthorized(Json.obj("message" -> "Not authorized"))
        }
      }
  }
}
