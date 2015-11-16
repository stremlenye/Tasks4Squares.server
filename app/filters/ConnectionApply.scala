package filters

import reactivemongo.api.DefaultDB
import scala.util.{Failure, Success, Try}

/**
  * Created by stremlenye on 16/11/15.
  */
trait ConnectionApply {
  def applyConnection[T](block: DefaultDB => T)(implicit db: Try[DefaultDB], onFailure: Throwable => T): T = db match {
    case Success(conn) => block(conn)
    case Failure(e) => onFailure(e)
  }
}
