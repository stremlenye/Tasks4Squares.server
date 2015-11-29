import entities.Implicits.TaskEntityLike
import models.Task
import org.specs2.Specification
import org.specs2.specification.core.SpecStructure
import reactivemongo.bson.{BSONInteger, BSONString, BSONDocument, BSONObjectID}

/**
  * Created by yuriyankudinov on 29/11/15.
  */
class TaskEntityLikeSpec extends Specification {
  override def is: SpecStructure =
    s2"""
       |TaskEntityLike should write object to BSONDocument $write
       |TaskEntityLike should read BSONDocument into Task $read
       |TaskEntityLike should read empty BSONDocument into None $readEmpty
      """.stripMargin

  def read = {
    val id = BSONObjectID.generate
    val doc = BSONDocument("_id" -> id, "text" -> "text", "priority" -> 1, "owner" -> "owner")
    val task = TaskEntityLike.reader.read(doc)
    task must beSome(Task(id.stringify, "text", 1, "owner"))
  }

  def readEmpty = {
    val doc = BSONDocument()
    val Task = TaskEntityLike.reader.read(doc)
    Task must beNone
  }

  def write = {
    val id = BSONObjectID.generate
    val token = Task(id.stringify, "Some Text", 1, "owner id")
    val doc = TaskEntityLike.writer.write(token)
    doc.get("_id") must beSome(id)
    doc.get("text") must beSome(BSONString("Some Text"))
    doc.get("priority") must beSome(BSONInteger(1))
    doc.get("owner") must beSome(BSONString("owner id"))
  }
}
