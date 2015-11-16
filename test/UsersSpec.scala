/**
  * Created by stremlenye on 12/11/15.
  */

import com.github.agourlay.cornichon.CornichonFeature
import play.api.test.{FakeApplication, TestServer}

class UsersSpec extends CornichonFeature {

  def feature =
    Feature("Signup") {
      Scenario("First signup") {implicit b ⇒
        When I POST("/signup", payload =
          """
          {
            "login": "test",
            "password": "test"
          }
          """
        )
        Then assert status_is(201)
        And assert body_is(whiteList = true, expected =
          """
          {
            "login": "test"
          }
          """)

        When I POST("/signup", payload =
          """
          {
            "login": "test",
            "password": "any other password"
          }
          """
        )
        Then assert status_is(400)
        And assert body_is(
          """
           {
             "message": "User with name given already exists"
           }
          """)
      }
    }

  lazy val port = 9000

  override lazy val baseUrl = s"http://localhost:$port"

  var server: TestServer = null

  beforeFeature {
    val app: FakeApplication =
      FakeApplication(
        additionalConfiguration = Map(
          "mongodb.uri" -> "mongodb://localhost:27017",
          "mongodb.db" -> "tasks"
        ))
    server = new TestServer(port, app)
    server.start
  }

  afterFeature {
    server.stop
  }
}

