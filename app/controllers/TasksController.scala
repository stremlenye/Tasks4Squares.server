package controllers

import javax.inject.Inject

import models.Task
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import stores.TasksStore
import scala.concurrent.Future

class TasksController @Inject() (tasksStore: TasksStore) extends Controller {

  implicit val taskReads: Reads[Task] = (__.read[Option[String]](None) and
    (__ \ "text").read[String] and
    (__ \ "priority").read[Int])(Task.apply _)

  implicit val taskWrites: Writes[Task] = ((__ \ "id").writeNullable[String] and
    (__ \ "text").write[String] and
    (__ \ "priority").write[Int])(unlift(Task.unapply))

  def list = Action.async(request => {
    Future {
      Ok
    }
  })

  def create = Action.async(request => {
    Future {
      Created
    }
  })

  def update(id: Int) = Action.async(request => {
    Future {
      Created
    }
  })

  def delete(id: Int) = Action.async(request => {
    Future {
      Ok
    }
  })
}
