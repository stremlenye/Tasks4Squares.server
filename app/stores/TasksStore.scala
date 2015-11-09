package stores

import javax.inject.Inject

import connection.Connection
import entities.Entity.EntityLike
import models.Task
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

/**
  * Created by stremlenye on 04/11/15.
  */
class TasksStore @Inject() (conn: Connection) extends CommonStore[Task](conn.db) with Creatable[Task]
  with Fetchable[Task, String] with Removable[Task, String] with Updatable[Task, String] {

  override val name: String = "tasks"

  override def identifier(id: String): BSONDocument = BSONDocument("id" -> id)

  def fetchByOwner(owner: String)(implicit el: EntityLike[Task]): Future[Option[Seq[Task]]] = {
    implicit val reader = el.reader
    collection.find(BSONDocument("owner" -> owner)).cursor[Option[Task]]().collect[Seq]() map {
      seq => if(seq.exists(item => item.isEmpty)) None else Some(seq.map(_.get))
    }
  }
}
