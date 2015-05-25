import helpers.Hashes
import org.junit.runner.RunWith
import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.Json
import play.api.mvc.Cookie

import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
 * Created by stremlenye on 26/11/14.
 */
@RunWith(classOf[JUnitRunner])
class AuthSpec extends Specification {
  "Auth API spec".title

  step(steps.createDb)

  val additionalConfiguration: Map[String, String] = Map(
    "mongodb.uri" -> (TestVariables.mongoProtocol + TestVariables.mongoUrl + TestVariables.dbName)
  )
  val application = FakeApplication(additionalConfiguration = additionalConfiguration)
  step(play.api.Play.start(application))


  "signin method" should {

    "answer OK on valid auth data and add token to the response body" in {
      val result = controllers.Auth.signIn()(FakeRequest("POST","/signin",
        FakeHeaders(Seq("Content-Type" -> Seq("application/json"))),
        Json.obj(
          "login" -> "precreated",
          "password" -> "12345")
      ))

      status(result) must equalTo(OK)
      cookies(result) get "token" must not beNull
    }

    "send bad request on request without auth information" in {
      val result = controllers.Auth.signIn()(FakeRequest("POST", "/signin")).run

      status(result) must equalTo(BAD_REQUEST)
    }

    "send Unauthorized status if credentials were wrong" in {
      val result = controllers.Auth.signIn()(FakeRequest("POST", "/signin",
        FakeHeaders(Seq("Content-Type" -> Seq("application/json"))),
        Json.obj(
          "login" -> "wrong_login",
          "password" -> "fake_password")
        ))

      status(result) must beEqualTo(UNAUTHORIZED)
      (contentAsJson(result) \ "message").as[String] must beEqualTo("Login or password is incorrect")
    }
  }

  "signout method" should {

    "answer with OK status for authenticated user" in {
      val result = controllers.Auth.signOut()(
        FakeRequest("POST", "/signout").withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
        )

      status(result) must beEqualTo(OK)
    }

    "answer with UNAUTHORIZED status for unauthenticated user" in {
      val result = controllers.Auth.signOut()(
        FakeRequest("POST", "/signout")
      )

      status(result) must beEqualTo(UNAUTHORIZED)
    }
  }

  step(play.api.Play.stop())
  step(steps.dropDb)

  object steps {
    /**
     * Create db for backend
     */
    def createDb = {
      val driver = MongoDriver()
      val db = driver.connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)

      val unauthenticated = db.collection[JSONCollection]("users").insert(
        Json.obj(
          "_id" -> BSONObjectID.generate.stringify,
          "name"->"precreated",
          "login" -> "precreated",
          "password" -> Hashes.sha("12345")
        )
      )
      val authenticated = db.collection[JSONCollection]("users").insert(
        Json.obj(
          "_id" -> BSONObjectID.generate.stringify,
          "name"->"authenticated",
          "login" -> "authenticated",
          "password" -> Hashes.sha("12345"),
          "token" -> TestVariables.randomUuid
        )
      )
      Await.ready(Future.sequence(Seq(unauthenticated, authenticated)),Duration.Inf)
      println("Db successfuly installed")
    }

    def dropDb = {
      val driver = MongoDriver()
      val db = driver.connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)
      val res = db.drop() map {
        lastError => success("Db succesefuly droped")
      } recover {
        case _ => failure("Failed to drop db")
      }
      Await.ready(res, Duration.Inf)
      println("Db successfuly dropped")
    }
  }
}
