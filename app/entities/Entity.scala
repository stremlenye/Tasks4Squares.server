package entities

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}

/**
  * Created by stremlenye on 04/11/15.
  */
object Entity {

  trait EntityLike[A] {
    implicit val writer: BSONDocumentWriter[A]
    implicit val reader: BSONDocumentReader[Option[A]]
  }

}
