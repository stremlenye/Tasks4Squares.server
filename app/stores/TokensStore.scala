package stores

import javax.inject.Inject

import connection.Connection
import models.Token
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocument

/**
  * Created by stremlenye on 04/11/15.
  */
class TokensStore @Inject() (conn: Connection) extends CommonStore[Token](conn.db) with Fetchable[Token, String]
  with Removable[Token, String] with Creatable[Token] {

  override val name: String = "tokens"

  override def identifier(id: String): BSONDocument = BSONDocument("token" -> id)
}
