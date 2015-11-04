package entities

import entities.Implicits._
import entities.Entity.EntityLike
import models.Token
import org.joda.time.DateTime
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader}

/**
  * Created by stremlenye on 04/11/15.
  */
class TokenEntityLike extends EntityLike[Token] {
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
