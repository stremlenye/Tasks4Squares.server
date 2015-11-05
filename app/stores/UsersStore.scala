package stores

import javax.inject.Inject

import connection.Connection
import entities.Entity.EntityLike
import models.User
import reactivemongo.bson.BSONDocument
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by stremlenye on 04/11/15.
  */
class UsersStore @Inject() (conn: Connection) extends CommonStore[User](conn.db) with Fetchable[User, String]
  with Creatable[User] {

  override def identifier(id: String): BSONDocument = BSONDocument("_id" -> id)

  override val name: String = "users"

  def find(login: String, password: String)(implicit el: EntityLike[User]): Future[Option[User]] = {
    implicit val reader = el.reader
    collection.find(BSONDocument("login" -> login, "password" -> password)).one[Option[User]].map(_.getOrElse(None))
  }
}
