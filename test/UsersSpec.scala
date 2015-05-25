import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api.MongoDriver

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class UsersSpec extends Specification {

  "Users API spec".title

  step(steps.createDb)
  "Users controller register method" should {
    val additionalConfiguration: Map[String, String] = Map(
      "mongodb.uri" -> (TestVariables.mongoProtocol + TestVariables.mongoUrl + TestVariables.dbName)
    )
    val application = FakeApplication(additionalConfiguration = additionalConfiguration)
    step(play.api.Play.start(application))

    "register user" in {
      val user = Json.obj("name" -> "Test User", "login" -> "test_user", "password" -> "12345")
      val result = controllers.Users.register()(FakeRequest("POST","/users/register",
        FakeHeaders(Seq("Content-Type" -> Seq("application/json"))),
        user))

      val r = status(result) must beEqualTo(CREATED)
      val content = contentAsJson(result)
      content must not beNull

      (content \ "id") must not beNull

      (content \ "name").as[String] must beEqualTo((user \ "name").as[String])
      (content \ "login").as[String] must beEqualTo((user \ "login").as[String])

      def getUser(id: String) = MongoDriver()
        .connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)
        .collection[JSONCollection]("users")
        .find(Json.obj("_id" -> id))
        .one[JsObject]

      (content \ "id").asOpt[String] match {
        case Some(id) => getUser(id) map {
          case Some(user) => success("User successfully added to database")
          case None => failure("User was not found in the db")
        }
        case None => failure("Something went wrong during obtaining user from db")
      }
      r
    }

    "send Conflict status if user already exist" in {
      val result = controllers.Users.register()(
        FakeRequest("POST", "/register",
          FakeHeaders(
            Seq("Content-Type" -> Seq("application/json"))
          ),
          Json.obj(
            "name" -> "precreated",
            "login" -> "precreated",
            "password" -> "12345"
          )
        )
      )
      status(result) must beEqualTo(CONFLICT)
      (contentAsJson(result) \ "message").as[String] must beEqualTo("User with same login already exists")
    }

    "send BadRequest status on currupted request body" in {
      val result = controllers.Users.register()(
        FakeRequest("POST","/register",
          FakeHeaders(
            Seq(
              "Content-Type" -> Seq("application/json")
            )
          ),
          Json.obj(
            "nothing" -> "interesting")
        )
      )
      status(result) must beEqualTo(BAD_REQUEST)
    }

    step(play.api.Play.stop())
  }

  step(steps.dropDb)

  object steps {
    def createDb = {
      val driver = MongoDriver()
      val db = driver.connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)

      val res = db.collection[JSONCollection]("users").insert(
        Json.obj(
          "name"->"precreated",
          "login" -> "precreated",
          "password" -> "12345"
        )
      )
      Await.result(res,Duration.Inf)
      println("Db successfuly installed")
    }

    def dropDb = {
      val driver = MongoDriver()
      val db = driver.connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)
      val res = db.drop()
      Await.result(res, Duration.Inf)
      println("Db successfuly dropped")
    }
  }


}