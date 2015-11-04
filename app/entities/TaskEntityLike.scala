package entities

import entities.Entity.EntityLike
import models.Task
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * Created by stremlenye on 04/11/15.
  */
class TaskEntityLike extends EntityLike[Task] {
  override implicit val writer: BSONDocumentWriter[Task] = new BSONDocumentWriter[Task] {
    override def write(t: Task): BSONDocument = BSONDocument(
      "text" -> t.text,
      "priority" -> t.priority
    )
  }

  override implicit val reader: BSONDocumentReader[Option[Task]] = new BSONDocumentReader[Option[Task]] {
    override def read(bson: BSONDocument): Option[Task] = for(
      id <- bson.getAs[String]("_id");
      text <- bson.getAs[String]("text");
      priority <- bson.getAs[Int]("priority")
    ) yield Task(Some(id), text, priority)
  }
}
