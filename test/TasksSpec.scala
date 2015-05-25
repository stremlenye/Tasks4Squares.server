/**
 * Created by stremlenye on 21/01/15.
 */

import helpers.Hashes
import org.json.JSONObject
import org.specs2.matcher.Matcher
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.mvc.Cookie

import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class TasksSpec  extends Specification {

  "Tasks API spec".title

  def getTask(id: String) = MongoDriver()
    .connection(Seq(TestVariables.mongoUrl))
    .db(TestVariables.dbName)
    .collection[JSONCollection]("tasks")
    .find(Json.obj("_id" -> id))
    .one[JsObject]



  val additionalConfiguration: Map[String, String] = Map(
    "mongodb.uri" -> (TestVariables.mongoProtocol + TestVariables.mongoUrl + TestVariables.dbName)
  )
  val application = FakeApplication(additionalConfiguration = additionalConfiguration)

  val token = Cookie("token", TestVariables.randomUuid)
  val authenticated = Cookie("authenticated", "true", None, "/", None, false, false)

  val jsonContentHeader = "Content-Type" -> "application/json"

  step(steps.createDb)
  step(play.api.Play.start(application))

  "GET /tasks" should {

    "return list of tasks" in {
      val result = controllers.Tasks.list()(
        FakeRequest(GET, "/tasks")
          .withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )

      val r = status(result) must beEqualTo(OK)
      val content = contentAsJson(result)
      content must not beNull

      content.asOpt[JsArray] match {
        case None => failure("Content is not JSON Array")
        case Some(array) => {
          array.value.length must not beLessThanOrEqualTo 0
          for (task <- array.value) {
            (task \ "id") must not be anInstanceOf[JsUndefined]
            (task \ "text") must not be anInstanceOf[JsUndefined]
            (task \ "priority") must not be anInstanceOf[JsUndefined]
          }
        }
      }
      r
    }

    "return Unauthorized response for request without token" in {
      val result = controllers.Tasks.list()(
        FakeRequest(GET, "/tasks")
      )

      status(result) must beEqualTo(UNAUTHORIZED)
    }
  }

  "POST /tasks" should {

    "add task to list" in {
      val result = controllers.Tasks.add()(
        FakeRequest(POST, "/tasks")
          .withHeaders(jsonContentHeader)
          .withBody(
            Json.obj(
              "text" -> "new task",
              "priority" -> 1
            )
          ).withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )

      val r = status(result) must beEqualTo(CREATED)
      val __ = contentAsJson(result)
      __ must not beNull

      (__ \ "url").asOpt[String] match {
        //TODO write regulare experession for return url
        case Some(url) => url must contain("/task/")
        case None => failure("task url was not passed as response")
      }

      (__ \ "id").asOpt[String] match {
        case Some(id) => getTask(id) map {
          case Some(task) => success("task was successfuly found in the db")
          case None => failure("task was not found in the db")
        }
        case None => failure("task id was not passed as response")
      }

      r
    }

    "return BadRequest response for corrupted request" in {

      val result = controllers.Tasks.add()(
        FakeRequest(POST, "/tasks")
          .withHeaders(jsonContentHeader)
          .withBody(
            Json.obj(
              "illegal_field" -> "new task",
              "another_strange_field" -> 1
            )
          ).withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )

      status(result) must beEqualTo(BAD_REQUEST)
    }

    "return UNAUTHORIZED response for request without token" in {
      val result = controllers.Tasks.add()(
        FakeRequest(POST, "/tasks")
          .withHeaders(jsonContentHeader)
          .withBody(
            Json.obj(
              "text" -> "new task",
              "priority" -> 1
            )
          )
      )

      status(result) must beEqualTo(UNAUTHORIZED)
    }
  }

  "PUT /tasks" should {

    "update task" in {
      val result = controllers.Tasks.update("for_edit") (
        FakeRequest(PUT, "/tasks/for_edit")
          .withHeaders(jsonContentHeader)
          .withBody(
            Json.obj(
              "text" -> "edit_successed",
              "priority" -> 2
            )
          ).withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )
      status(result) must beEqualTo(OK)
    }

    "return NOT_FOUND for attempt to edit alien task" in {
      val result = controllers.Tasks.update("alien_for_edit") (
        FakeRequest(PUT, "/tasks/alien_for_edit")
          .withHeaders(jsonContentHeader)
          .withBody(
            Json.obj(
              "text" -> "edit_successed",
              "priority" -> 2
            )
          ).withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )
      status(result) must beEqualTo(NOT_FOUND)
    }

    "return UNAUTHORIZED response for request without token" in {
      val result = controllers.Tasks.update("for_edit")(
        FakeRequest(PUT, "/tasks")
          .withHeaders(jsonContentHeader)
          .withBody(
            Json.obj(
              "text" -> "edit_successed",
              "priority" -> 1
            )
          )
      )

      status(result) must beEqualTo(UNAUTHORIZED)
    }
  }

  "DELETE /tasks" should {

    "delete task from list" in {
      val id = "for_delete"
      val result = controllers.Tasks.delete(id)(
        FakeRequest(DELETE, "/tasks/" + id)
          .withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )

      getTask(id) map {
        case Some(task) => failure("task wasn't deleted from db")
        case None => success("task was successfully deleted from db")
      }

      status(result) must beEqualTo(OK)
    }

    "return NOT_FOUND for attempt to delete alien task" in {
      val id = "alien_for_delete"
      val result = controllers.Tasks.delete(id)(
        FakeRequest(DELETE, "/tasks/" + id)
          .withHeaders(
            "Authorization" -> ("Token " + TestVariables.randomUuid)
          )
      )

      getTask(id) map {
        case Some(task) => success("task wasn't deleted from db")
        case None => failure("task was deleted from db")
      }

      status(result) must beEqualTo(NOT_FOUND)
    }

    "return UNAUTHORIZED response for request without token" in {
      val id = "for_delete"
      val result = controllers.Tasks.delete(id)(
        FakeRequest(DELETE, "/tasks/" + id)
          .withHeaders(jsonContentHeader)

      )

      getTask(id) map {
        case Some(task) => success("task wasn't deleted from db")
        case None => failure("task was deleted from db")
      }
      
      status(result) must beEqualTo(UNAUTHORIZED)
    }
  }

  step(play.api.Play.stop())
  step(steps.dropDb)

  object steps {
    def createDb = {
      val driver = MongoDriver()
      val db = driver.connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)

      val ops =  db.collection[JSONCollection]("users").insert(
        Json.obj(
          "_id" -> BSONObjectID.generate.stringify,
          "name"->"precreated",
          "login" -> "precreated",
          "password" -> Hashes.sha("12345")
        )
      ) :: db.collection[JSONCollection]("users").insert(
        Json.obj(
          "_id" -> "authenticated",
          "name"->"authenticated",
          "login" -> "authenticated",
          "password" -> Hashes.sha("12345"),
          "token" -> TestVariables.randomUuid
        )
      ) :: db.collection[JSONCollection]("tasks").insert(
        Json.obj(
          "_id" -> "for_edit",
          "text" -> "for edit",
          "priority" -> 1,
          "owner" -> "authenticated"
        )
      ) :: db.collection[JSONCollection]("tasks").insert(
        Json.obj(
          "_id" -> "alien_for_edit",
          "text" -> "alien_for edit",
          "priority" -> 1,
          "owner" -> "alien"
        )
      ) :: db.collection[JSONCollection]("tasks").insert(
        Json.obj(
          "_id" -> "for_delete",
          "text" -> "for delete",
          "priority" -> 1,
          "owner" -> "authenticated"
        )
      ) :: db.collection[JSONCollection]("tasks").insert(
        Json.obj(
          "_id" -> "alien_for_delete",
          "text" -> "alien_for delete",
          "priority" -> 1,
          "owner" -> "alien"
        )
      ) :: List()

      Await.ready(Future.sequence(ops),Duration.Inf) map {
         ls => println("Db successfully installed")
      } recover {
        case _ => {
          println("Failed to install DB")
          failure("Failed to install DB")
        }
      }
    }

    def dropDb = {
      val driver = MongoDriver()
      val db = driver.connection(Seq(TestVariables.mongoUrl)).db(TestVariables.dbName)
      val res = db.drop() map {
        lastError => success("Db successfully dropped")
      } recover {
        case _ => failure("Failed to drop db")
      }
      Await.ready(res, Duration.Inf) map {
        ls => println("Db successfully dropped")
      } recover {
        case _ => {
          println("Failed to drop DB")
          failure("Failed to drop DB")
        }
      }
    }
  }
}
