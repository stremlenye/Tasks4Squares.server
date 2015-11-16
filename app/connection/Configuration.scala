package connection

import play.Play._

/**
  * Created by stremlenye on 05/11/15.
  */
object Configuration {
  def mongoDbUri(): String = application().configuration().getString("mongodb.uri")
  def mongoDefaultDb(): String = application().configuration().getString("mongodb.db")
}
