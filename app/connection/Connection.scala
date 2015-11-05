package connection



import javax.inject.Singleton

import play.Play._
import reactivemongo.api.MongoConnection.ParsedURI

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

/**
  * Created by stremlenye on 05/11/15.
  */
@Singleton
class Connection {
  val db: DefaultDB = MongoConnection.parseURI(Configuration.mongoDbUri) map {
    parsedUri => (new MongoDriver)
      .connection(parsedUri)
      .db(parsedUri.db.getOrElse(Configuration.mongoDefaultDb))
  } get
}
