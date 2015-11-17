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
  private var server: TestServer = _

  val port = 9000

  def start(): Unit = {
    if(server == null) {
      server = new TestServer(port, app)
      server.start()
    }
  }

  def stop(): Unit = {
    val serverToStop = server
    server = null
    if(serverToStop != null) {
      serverToStop.stop()
    }
  }

}
