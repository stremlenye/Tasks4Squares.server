package stores

import models.Token
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocument

/**
  * Created by stremlenye on 04/11/15.
  */
class TokensStore(db: DefaultDB) extends CommonStore[Token](db) with Fetchable[Token, String]
  with Removable[Token, String] with Creatable[Token] {

  override val name: String = "tokens"

  override def identifier(id: String): BSONDocument = BSONDocument("token" -> id)
}
