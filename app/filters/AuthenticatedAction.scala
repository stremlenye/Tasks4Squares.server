package filters

import connection.Connection
import entities.Implicits._
import models.User
import scala.concurrent.ExecutionContext.Implicits._
import play.api.mvc._
import play.mvc.Http.HeaderNames
import stores.{TokensStore, UsersStore}

import scala.concurrent.Future

abstract class CommonRequest[A]
case class AuthenticatedRequest[A] (user: User, request: Request[A]) extends CommonRequest[A]
case class UnauthenticatedRequest[A] (request: Request[A]) extends CommonRequest[A]

/**
  * Created by stremlenye on 08/11/15.
  */
object AuthenticatedAction extends ActionBuilder[CommonRequest] {

  private val connection = new Connection

  val usersStore = new UsersStore(connection)
  val tokensStore = new TokensStore(connection)

  override def invokeBlock[A](request: Request[A],
                              block: (CommonRequest[A]) => Future[Result]): Future[Result] ={
    def fetchUserData(token: String): Future[Option[User]] =
      tokensStore.fetch(token).flatMap {
        case Some(token) => usersStore.fetch(token.owner)
        case _ => Future(None)
      }
    request.headers.get(HeaderNames.AUTHORIZATION).map(fetchUserData _).map(_ map {
      case Some(user) => AuthenticatedRequest(user, request)
      case _ => UnauthenticatedRequest(request)
    }).map(_ flatMap (r => block(r))).get
  }

}
