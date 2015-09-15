package controllers

import play.api.mvc._

import scala.concurrent.Future

class Tasks extends Controller {

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

  def update = Action.async(request => {
    Future {
      Created
    }
  })

  def delete = Action.async(request => {
    Future {
      Ok
    }
  })
}
