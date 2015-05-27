package controllers

import controllers.Auth.Authenticated
import models.Task
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson._
import reactivemongo.core.commands.{Aggregate, Match, Project}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Created by stremlenye on 14/11/14.
 * Tasks controller
 */
object Tasks extends Controller with MongoController {
  var tasks = db.collection[JSONCollection]("tasks")

  case class TaskForm(val text: String, val priority: Int)

  implicit val taskFormFormat: Format[TaskForm] = Json.format[TaskForm]

  implicit val taskFormat: Format[Task] = Format(((__ \ "_id").read[String] and
    (__ \ "text").read[String] and
    (__ \ "priority").read[Int] and
    (__ \ "owner").read[String])(Task.apply _),
    ((__ \ "_id").write[String] and
      (__ \ "text").write[String] and
      (__ \ "priority").write[Int] and
      (__ \ "owner").write[String])(unlift(Task.unapply)))

  def list = Authenticated.async {
    request => {
      val command = Aggregate("tasks", Seq(
        Match(BSONDocument("owner" -> BSONString(request.user.id))),
        Project(
          "_id" -> BSONBoolean(false),
          "id" -> BSONString("$_id"),
          "text" -> BSONBoolean(true),
          "priority" -> BSONBoolean(true)
        )
      )
      )
      db.command(command) map {
        stream => stream.toSeq
      } map {
        seq => seq.map(BSONFormats.toJSON(_))
      } map {
        jsonList => Ok(Json.toJson(jsonList))
      } recover {
        case _ => InternalServerError(Json.obj("message" -> "Couldn't load tasks list"))
      }
    }
  }

  /*
   * POST /tasks method
   * Adds task to users tasks collection
   */
  def add = Authenticated.async(parse.json) {
    request => request.body.validate[TaskForm]
      .fold(invalid => Future{ BadRequest },
      t => {
        val id = BSONObjectID.generate.stringify
        val task = new Task(id, t.text, t.priority, request.user.id)
        tasks.insert(task) map {
          lastError => lastError.ok match {
            case false => InternalServerError("Saving new task failed")
            case _ => Created(Json.obj("id" -> id, "url" -> ("/task/" + id)))
          }
        }
      }
    )
  }

  def update(id: String) = Authenticated.async(parse.json) {
    request => request.body.validate[TaskForm].fold(
      invalid => Future {
        BadRequest
      },
      task => tasks.update(Json.obj(
        "_id" -> id,
        "owner" -> request.user.id
      ),
        Json.obj(
          "$set" -> Json.obj(
            "text" -> task.text,
            "priority" -> task.priority)
        )
      ) map { lastError => lastError.ok match {
        case false => InternalServerError("Failed to update task")
        case _ => lastError.updated match {
            case 0 => NotFound
            case 1 => Ok
          }
        }
      }
    )
  }

  def delete(id: String) = Authenticated.async {
    request => tasks.remove(Json.obj("_id" -> id, "owner" -> request.user.id)) map {
        lastError => lastError.ok match {
          case false => InternalServerError("Failed to delete task")
          case _ => lastError.updated match {
            case 0 => NotFound
            case 1 => Ok
          }
        }
    }
  }
}
