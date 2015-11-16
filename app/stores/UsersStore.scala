package stores

import connection.Connection
import entities.Entity.EntityLike
import models.User
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocument
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by stremlenye on 04/11/15.
  */
class UsersStore (db: DefaultDB) extends CommonStore[User](db) with Fetchable[User, String]
  with Creatable[User] {

  override def identifier(id: String): BSONDocument = BSONDocument("_id" -> id)

  override val name: String = "users"

  def find(login: String, password: String)(implicit el: EntityLike[User]): Future[Option[User]] = {
    implicit val reader = el.reader
    collection.find(BSONDocument("login" -> login, "password" -> password)).one[Option[User]].map(_.getOrElse(None))
  }

  def find(login: String)(implicit el: EntityLike[User]): Future[Option[User]] = {
    implicit val reader = el.reader
    collection.find(BSONDocument("login" -> login)).one[Option[User]].map(_.getOrElse(None))
  }
}

object UsersStore {
  def apply(db: DefaultDB): UsersStore = new UsersStore(db)
}