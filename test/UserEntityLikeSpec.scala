import entities.Implicits.UserEntityLike
import models.User
import org.specs2.Specification
import org.specs2.specification.core.SpecStructure
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}

/**
  * Created by yuriyankudinov on 29/11/15.
  */
class UserEntityLikeSpec extends Specification {
  override def is: SpecStructure =
    s2"""
        |UserEntityLike should write object to BSONDocument $write
        |UserEntityLike should read BSONDocument into User $read
        |UserEntityLike should read empty BSONDocument into None $readEmpty
      """.stripMargin

  def read = {
    val id = BSONObjectID.generate
    val doc = BSONDocument("_id" -> id, "login" -> "login", "password" -> "password")
    val user = UserEntityLike.reader.read(doc)
    user must beSome(User(id.stringify, "login", None))
  }

  def readEmpty = {
    val doc = BSONDocument()
    val user = UserEntityLike.reader.read(doc)
    user must beNone
  }

  def write = {
    val token = User(BSONObjectID.generate.stringify, "login", Some("password"))
    val doc = UserEntityLike.writer.write(token)
    doc.get("login") must beSome(BSONString("login"))
    doc.get("password") must beSome(BSONString("password"))
  }
}
