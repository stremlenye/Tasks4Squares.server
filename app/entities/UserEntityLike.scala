package entities

import entities.Entity.EntityLike
import models.User
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * Created by stremlenye on 04/11/15.
  */
class UserEntityLike extends EntityLike[User] {
  override implicit val writer: BSONDocumentWriter[User] = new BSONDocumentWriter[User] {
    override def write(t: User): BSONDocument = BSONDocument(
      "login" -> t.login,
      "password" -> t.password
    )
  }
  override implicit val reader: BSONDocumentReader[Option[User]] = new BSONDocumentReader[Option[User]] {
    override def read(bson: BSONDocument): Option[User] = for (
      id <- bson.getAs[String]("_id");
      login <- bson.getAs[String]("login")
    ) yield User(Some(id), login, None)
  }
}
