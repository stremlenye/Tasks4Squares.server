import com.github.agourlay.cornichon.CornichonFeature
import com.github.agourlay.cornichon.core.FeatureDef
import helpers.TestServerInstance
import scala.concurrent.duration._

/**
  * Created by stremlenye on 18/11/15.
  */
class TasksSpec extends CornichonFeature {

  override def feature: FeatureDef = Feature("Tasks"){
    Scenario("work with task") { implicit b =>
      When I POST("/signup", payload =
        """
          {
            "login": "test2",
            "password": "test2"
          }
        """
      )
      Then assert status_is(201)
      And assert body_is(whiteList = true, expected =
        """
          {
            "login": "test2"
          }
        """)
      And I save_from_body(_ \ "id", "owner")

      When I POST("/tokens", payload =
        """
            {
              "login": "test2",
              "password": "test2"
            }
        """)
      Then assert status_is(201)
      And assert body_is(whiteList = true,
        """
            {
              "owner": "<owner>"
            }
        """)
      And I save_from_body(_ \ "token", "token")
      And debug show_last_response_body

      When I POST("/tasks", payload =
        """
          {
            "text": "test task",
            "priority": 1
          }
        """)(headers = Seq("Authorization" -> "<token>"))
      Then assert status_is(201)
      And assert body_is(whiteList = true,
        """
          {
            "text": "test task",
            "priority": 1,
            "owner": "<owner">
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
