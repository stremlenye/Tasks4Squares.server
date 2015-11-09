package helpers

import reactivemongo.bson.BSONObjectID

/**
  * Created by stremlenye on 09/11/15.
  */
object Id {
  def generate: String = BSONObjectID.generate.toString()
}
