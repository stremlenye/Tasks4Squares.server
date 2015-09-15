package controllers

import play.api.mvc._

import scala.concurrent.Future

/**
 * Created by yankudinov on 15/09/15.
 */
class Users extends Controller {
  def create = Action.async(request => {
    Future {
      Ok
    }
  })
}
