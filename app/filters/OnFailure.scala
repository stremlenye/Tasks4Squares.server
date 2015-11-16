package filters

import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by stremlenye on 16/11/15.
  */
trait OnFailure {
  implicit def onFailure(e: Throwable): Future[Result] = Future {
    new Status(500)(Json.obj("message" -> e.getMessage))
  }
}
