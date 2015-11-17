/**
  * Created by stremlenye on 08/11/15.
  */

import filters.CORS
import play.api._
import play.api.mvc.WithFilters

object Global extends WithFilters(CORS) {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application): Unit = {
    Logger.info("Application has stopped")
  }
}