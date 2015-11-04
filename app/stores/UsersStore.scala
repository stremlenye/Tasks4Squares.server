package stores

import models.User
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocument

/**
  * Created by stremlenye on 04/11/15.
  */
class UsersStore(db: DefaultDB) extends CommonStore[User](db) with Fetchable[User, String] with Creatable[User] {


  override def identifier(id: String): BSONDocument = BSONDocument("_id" -> id)

  override val name: String = "users"
}
