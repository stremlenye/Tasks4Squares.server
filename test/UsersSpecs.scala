import controllers.UsersController
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

/**
 * Created by yankudinov on 15/09/15.
 */
@RunWith(classOf[JUnitRunner])
class UsersSpecs extends Specification {
  """Users controller""" should {
    """allow to create user""" in {
      val usersController = new UsersController
      val result = usersController.create()
    }
  }
}
