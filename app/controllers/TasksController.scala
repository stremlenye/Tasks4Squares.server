package controllers

import connection.{Configuration, Connection}
import filters.{OnFailure, ConnectionApply, AuthenticatedAction, AuthenticatedRequest}
import helpers.Id
import models.Task
import scala.concurrent.ExecutionContext.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import stores.TasksStore
import entities.Implicits._

import scala.concurrent.Future

case class TaskForm(text: String, priority: Int)

class TasksController extends Controller with Connection
  with ConnectionApply with OnFailure {

  override val mongoDbUri: String = Configuration.mongoDbUri()
  override val mongoDefaultDb: String = Configuration.mongoDefaultDb()

  implicit val taskFormReader: Reads[TaskForm] = ((__ \ "text").read[String] and
    (__ \ "priority").read[Int])(TaskForm.apply _)

  implicit val taskWrites: Writes[Task] = ((__ \ "id").write[String] and
    (__ \ "text").write[String] and
    (__ \ "priority").write[Int] and
    (__ \ "owner").write[String])(unlift(Task.unapply))

  def list = AuthenticatedAction.async(parse.empty) {
    case request: AuthenticatedRequest[Unit] => applyConnection(db => TasksStore(db).fetchByOwner(request.user.id) map {
      case Some(tasks) => Ok(Json.toJson(tasks))
      case _ => NotFound
    })
    case _ => Future(Unauthorized)
  }

  def create = AuthenticatedAction.async(parse.json[TaskForm]) {
    case r: AuthenticatedRequest[TaskForm] => {
      val task = Task(Id.generate, r.request.body.text, r.request.body.priority, r.user.id)
      applyConnection(db => TasksStore(db).create(task).map({
        case 1 => Created(Json.toJson(task))
        case _ => InternalServerError(Json.obj("message" -> "Can't save task"))
      }))
    }
    case _ => Future(Unauthorized)
  }

  def update(id: String) = AuthenticatedAction.async(parse.json[TaskForm]) {
    case r: AuthenticatedRequest[TaskForm] => applyConnection(db => TasksStore(db).fetch(id) flatMap {
      case Some(Task(id, text, priority, owner)) if owner == r.user.id => {
        val task = Task(id, r.request.body.text, r.request.body.priority, owner)
        TasksStore(db).update(id, task) map {
          case 1 => Ok(Json.toJson(task))
          case _ => InternalServerError(Json.obj("message" -> "Can't save task"))
        }
      }
      case _ => Future(NotFound)
    })
    case _ => Future(Unauthorized)
  }

  def delete(id: String) = AuthenticatedAction.async(parse.empty) {
    case r: AuthenticatedRequest[Unit] => applyConnection(db => TasksStore(db).fetch(id) flatMap {
      case Some(task) => TasksStore(db).remove(id) map {
        case 1 => Ok(Json.toJson(task))
        case _ => InternalServerError(Json.obj("message" -> "Can't remove task"))
      }
      case None => Future(NotFound)
    })
    case _ => Future(Unauthorized)
  }
}
