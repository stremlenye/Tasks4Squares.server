package entities

import entities.Entity.EntityLike
import models.{User, Token, Task}
import org.joda.time.DateTime
import reactivemongo.bson._

/**
  * Created by stremlenye on 04/11/15.
  */

object Implicits {

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }

  implicit object TaskEntityLike extends EntityLike[Task] {
    override implicit val writer: BSONDocumentWriter[Task] = new BSONDocumentWriter[Task] {
      override def write(t: Task): BSONDocument = BSONDocument(
        "_id" -> BSONObjectID.apply(t.id), //TODO: Not safe
        "text" -> t.text,
        "priority" -> t.priority,
        "owner" -> t.owner
      )
    }

    override implicit val reader: BSONDocumentReader[Option[Task]] = new BSONDocumentReader[Option[Task]] {
      override def read(bson: BSONDocument): Option[Task] = for(
        id <- bson.getAs[BSONObjectID]("_id").map(_.stringify);
        text <- bson.getAs[String]("text");
        priority <- bson.getAs[Int]("priority");
        owner <- bson.getAs[String]("owner")
      ) yield Task(id, text, priority, owner)
    }
  }

  implicit object TokenEntityLike extends EntityLike[Token] {
    override implicit val writer: BSONDocumentWriter[Token] = new BSONDocumentWriter[Token] {
      override def write(t: Token): BSONDocument = BSONDocument(
        "token" -> t.token,
        "owner" -> t.owner,
        "issuedAt" -> t.issuedAt
      )
    }

    override implicit val reader: BSONDocumentReader[Option[Token]] = new BSONDocumentReader[Option[Token]] {
      override def read(bson: BSONDocument): Option[Token] = for(
        token <- bson.getAs[String]("token");
        owner <- bson.getAs[String]("owner");
        issuedAt <- bson.getAs[DateTime]("issuedAt")
      ) yield Token(token, owner, issuedAt)
    }
  }

  implicit object UserEntityLike extends EntityLike[User] {
    override implicit val writer: BSONDocumentWriter[User] = new BSONDocumentWriter[User] {
      override def write(t: User): BSONDocument = BSONDocument(
        "_id" -> BSONObjectID.apply(t.id), //TODO: Not safe
        "login" -> t.login,
        "password" -> t.password
      )
    }
    override implicit val reader: BSONDocumentReader[Option[User]] = new BSONDocumentReader[Option[User]] {
      override def read(bson: BSONDocument): Option[User] = for (
        id <- bson.getAs[BSONObjectID]("_id").map(_.stringify);
        login <- bson.getAs[String]("login")
      ) yield User(id, login, None)
    }
  }

}
