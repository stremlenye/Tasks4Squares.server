package controllers

import javax.inject.Inject

import filters.AuthenticatedAction
import models.Task
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import stores.{UsersStore, TokensStore, TasksStore}
import scala.concurrent.Future

class TasksController @Inject() (tasksStore: TasksStore, tokensStore: TokensStore, usersStore: UsersStore) extends Controller {

  implicit val taskReads: Reads[Task] = (__.read[Option[String]](None) and
    (__ \ "text").read[String] and
    (__ \ "priority").read[Int] and
    (__ \ "owner").read[String])(Task.apply _)

  implicit val taskWrites: Writes[Task] = ((__ \ "id").writeNullable[String] and
    (__ \ "text").write[String] and
    (__ \ "priority").write[Int] and
    (__ \ "owner").write[String])(unlift(Task.unapply))

  def list = AuthenticatedAction.async(request => {
    Future {
      Ok
    }
  })

  def create = AuthenticatedAction.async(request => {
    Future {
      Created
    }
  })

  def update(id: Int) = AuthenticatedAction.async(request => {
    Future {
      Created
    }
  })

  def delete(id: Int) = AuthenticatedAction.async(request => {
    Future {
      Ok
    }
  })
}
