package controllers

import javax.inject.Inject

import filters.{AuthenticatedAction, AuthenticatedRequest}
import helpers.Id
import models.Task
import scala.concurrent.ExecutionContext.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import stores.TasksStore
import entities.Implicits._

import scala.concurrent.Future

class TasksController @Inject() (tasksStore: TasksStore) extends Controller {

  case class TaskForm(text: String, priority: Int)

  implicit val taskFormReader: Reads[TaskForm] = ((__ \ "text").read[String] and
    (__ \ "priority").read[Int])(TaskForm.apply _)

  implicit val taskWrites: Writes[Task] = ((__ \ "id").write[String] and
    (__ \ "text").write[String] and
    (__ \ "priority").write[Int] and
    (__ \ "owner").write[String])(unlift(Task.unapply))

  def list = AuthenticatedAction.async(parse.empty) {
    case request: AuthenticatedRequest[Unit] => tasksStore.fetchByOwner(request.user.id) map {
      case Some(tasks) => Ok(Json.toJson(tasks))
      case _ => NotFound
    }
    case _ => Future(Unauthorized)
  }

  def create = AuthenticatedAction.async(parse.json[TaskForm]) {
    case r: AuthenticatedRequest[TaskForm] => {
      val task = Task(Id.generate, r.request.body.text, r.request.body.priority, r.user.id)
      tasksStore.create(task).map({
        case 1 => Created(Json.toJson(task))
        case _ => InternalServerError(Json.obj("message" -> "Can't save task"))
      })
    }
    case _ => Future(Unauthorized)
  }

  def update(id: String) = AuthenticatedAction.async(parse.json[TaskForm]) {
    case r: AuthenticatedRequest[TaskForm] => tasksStore.fetch(id) flatMap {
      case Some(Task(id, text, priority, owner)) if owner == r.user.id => {
        val task = Task(id, r.request.body.text, r.request.body.priority, owner)
        tasksStore.update(id, task) map {
          case 1 => Ok(Json.toJson(task))
          case _ => InternalServerError(Json.obj("message" -> "Can't save task"))
        }
      }
      case _ => Future(NotFound)
    }
    case _ => Future(Unauthorized)
  }

  def delete(id: String) = AuthenticatedAction.async(parse.empty) {
    case r: AuthenticatedRequest[Unit] => tasksStore.fetch(id) flatMap {
      case Some(task) => tasksStore.remove(id) map {
        case 1 => Ok(Json.toJson(task))
        case _ => InternalServerError(Json.obj("message" -> "Can't remove task"))
      }
      case None => Future(NotFound)
    }
    case _ => Future(Unauthorized)
  }
}
