import com.github.nscala_time.time.Imports._
import entities.Implicits.TokenEntityLike
import models.Token
import org.specs2.Specification
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONString}

/**
  * Created by yuriyankudinov on 27/11/15.
  */
class TokenEntityLikeSpec extends Specification {

  def is = s2"""
      |TokenEntityLikeSpec should read BSONDocument $read
      |TokenEntityLikeSpec should write BSONDocument $write
    """.stripMargin

  def read = {
    val doc = BSONDocument("token" -> "blah", "owner" -> "owner", "issuedAt" -> BSONDateTime(1000))
    val token = TokenEntityLike.reader.read(doc)
    token must beSome(Token("blah", "owner", new DateTime(1000)))
  }

  def write = {
    val token = Token("blah", "owner", new DateTime(1000))
    val doc = TokenEntityLike.writer.write(token)
    doc.get("token") must beSome(BSONString("blah"))
    doc.get("owner") must beSome(BSONString("owner"))
  }
}
