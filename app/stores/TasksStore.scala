package stores

import javax.inject.Inject

import connection.Connection
import models.Task
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocument

/**
  * Created by stremlenye on 04/11/15.
  */
class TasksStore @Inject() (conn: Connection) extends CommonStore[Task](conn.db) with Creatable[Task]
  with Fetchable[Task, String] with Removable[Task, String] with Updatable[Task, String] {

  override val name: String = "tasks"

  override def identifier(id: String): BSONDocument = BSONDocument("id" -> id)
}
