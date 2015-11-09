package filters

import play.api.http.HeaderNames._
import play.api.mvc.Results._
import play.api.mvc.{Result, RequestHeader, Filter}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

object CORS extends Filter {
  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val origin = request.headers.get(ORIGIN).getOrElse("*")
    if (request.method == "OPTIONS") {
      val response = Ok.withHeaders(
        ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
        ACCESS_CONTROL_ALLOW_METHODS -> "POST, GET, OPTIONS, PUT, DELETE",
        ACCESS_CONTROL_MAX_AGE -> "3600",
        ACCESS_CONTROL_ALLOW_HEADERS -> s"$ORIGIN, X-Requested-With, $CONTENT_TYPE, $ACCEPT, $AUTHORIZATION, X-Auth-Token",
        ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
      )
      Future.successful(response)
    } else {
      next(request).map {
        res => res.withHeaders(
          ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
          ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
        )
      }
    }
  }
}
