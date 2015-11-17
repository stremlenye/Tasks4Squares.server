package filters

import connection.{Configuration, Connection}
import entities.Implicits._
import models.User
import play.api.mvc._
import play.mvc.Http.HeaderNames
import stores.{TokensStore, UsersStore}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

abstract class CommonRequest[A]
case class AuthenticatedRequest[A] (user: User, request: Request[A]) extends CommonRequest[A]
case class UnauthenticatedRequest[A] (request: Request[A]) extends CommonRequest[A]

/**
  * Created by stremlenye on 08/11/15.
  */
object AuthenticatedAction extends ActionBuilder[CommonRequest] with Connection
  with ConnectionApply with OnFailure {

  override val mongoDbUri: String = Configuration.mongoDbUri()
  override val mongoDefaultDb: String = Configuration.mongoDefaultDb()

  override def invokeBlock[A](request: Request[A],
                              block: (CommonRequest[A]) => Future[Result]): Future[Result] =
  applyConnection(db => {
    def fetchUserData(token: String): Future[Option[User]] = TokensStore(db).fetch(token).flatMap {
        case Some(token) => UsersStore(db).fetch(token.owner)
        case _ => Future(None)
      }
   (request.headers.get(HeaderNames.AUTHORIZATION).map(fetchUserData _) match {
      case Some(v) => v
      case None => Future(None)
    }) map {
     case Some(user) => AuthenticatedRequest(user, request)
     case None => UnauthenticatedRequest(request)
   } flatMap(r => block(r))
  })
}
