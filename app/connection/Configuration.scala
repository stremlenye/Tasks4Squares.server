package connection

import play.Play._

/**
  * Created by stremlenye on 05/11/15.
  */
object Configuration {
  def mongoDbUri() = application().configuration().getString("mongodb.uri")
  def mongoDefaultDb() = application().configuration().getString("mongodb.db")
}
