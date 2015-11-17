import com.github.agourlay.cornichon.CornichonFeature
import com.github.agourlay.cornichon.core.FeatureDef
import helpers.TestServerInstance

/**
  * Created by stremlenye on 17/11/15.
  */
class TokenSpec extends CornichonFeature {
  override def feature: FeatureDef =
    Feature("Signin") {
      Scenario("Simple signin") { implicit b ⇒
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
        And I save_from_body(_ \ "id", "owner")

        When I POST("/tokens", payload =
          """
            {
              "login": "test",
              "password": "test"
            }
          """)
        Then assert status_is(201)
        And assert body_is(whiteList = true,
          """
            {
              "owner": "<owner>"
            }
          """)
      }
    }

  lazy val port = 9000

  override lazy val baseUrl = s"http://localhost:$port"

  beforeFeature {
    TestServerInstance.start
  }

  afterFeature {
    TestServerInstance.stop
  }
}
