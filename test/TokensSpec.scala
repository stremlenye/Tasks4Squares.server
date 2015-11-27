import com.github.agourlay.cornichon.CornichonFeature
import com.github.agourlay.cornichon.core.FeatureDef
import helpers.TestServerInstance
import scala.concurrent.duration._

/**
  * Created by stremlenye on 17/11/15.
  */
class TokensSpec extends CornichonFeature {
  override def feature: FeatureDef =
    Feature("Signin") { implicit scenarioBuilder =>
      Scenario("Simple signin") { implicit stepBuilder =>
        When I POST("/signup", payload =
          """
          {
            "login": "test1",
            "password": "test1"
          }
          """
        )
        Then assert status_is(201)
        And assert body_is(whiteList = true, expected =
          """
          {
            "login": "test1"
          }
          """)
        And I save_from_body(_ \ "id", "owner")

        When I POST("/tokens", payload =
          """
            {
              "login": "test1",
              "password": "test1"
            }
          """)
        Then assert status_is(201)
        And assert body_is(whiteList = true,
          """
            {
              "owner": "<owner>"
            }
          """)

        When I POST("/tokens", payload =
          """
            {
              "login": "test1",
              "password": "wrong_password"
            }
          """)
        Then assert status_is(400)
        And assert body_is(
          """
            {
              "message": "No such user"
            }
          """)
      }
    }

  override lazy val baseUrl = s"http://localhost:${TestServerInstance.port}"

  override lazy val requestTimeout = 10 seconds

  beforeFeature {
    TestServerInstance.start
  }
}
