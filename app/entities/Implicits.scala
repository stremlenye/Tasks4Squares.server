package entities

import org.joda.time.DateTime
import reactivemongo.bson.{BSONHandler, BSONDateTime}

/**
  * Created by stremlenye on 04/11/15.
  */

object Implicits {
  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }
}
