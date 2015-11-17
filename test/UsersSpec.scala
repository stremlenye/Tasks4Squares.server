/**
  * Created by stremlenye on 12/11/15.
  */

import com.github.agourlay.cornichon.CornichonFeature
import helpers.TestServerInstance
import scala.concurrent.duration._

class UsersSpec extends CornichonFeature {

  def feature =
    Feature("Signup") {
      Scenario("First signup") { implicit b ⇒
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

  override lazy val baseUrl = s"http://localhost:${TestServerInstance.port}"

  override lazy val requestTimeout = 10 seconds

  beforeFeature {
    TestServerInstance.start
  }

  afterFeature {
    TestServerInstance.stop
  }
}

