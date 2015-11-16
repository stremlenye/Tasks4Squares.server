package connection

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.util.Try

/**
  * Created by stremlenye on 05/11/15.
  */
trait Connection {

  val mongoDbUri: String
  val mongoDefaultDb: String

  implicit lazy val db: Try[DefaultDB] = MongoConnection.parseURI(mongoDbUri) map {
    parsedUri => (new MongoDriver)
      .connection(parsedUri)
      .db(parsedUri.db.getOrElse(mongoDefaultDb))
  }
}