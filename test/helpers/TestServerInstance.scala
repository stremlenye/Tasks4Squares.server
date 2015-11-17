package helpers

import org.joda.time.DateTime
import play.api.test.{TestServer, FakeApplication}

/**
  * Created by stremlenye on 17/11/15.
  */
object TestServerInstance {
  private val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map(
        "mongodb.uri" -> "mongodb://127.0.0.1:27017",
        "mongodb.db" -> ("tasks_" + DateTime.now().getMillis.toString)
      ))
  private val server = new TestServer(9000, app)

  def start(): Unit = server.start()

  def stop(): Unit = server.stop()

}
