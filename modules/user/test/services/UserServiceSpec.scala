package services

import config.TestContext
import play.api.Play

/**
  * @author im.
  */
class UserServiceSpec extends TestContext {

  "Test UserService" must {

//    "create user" in {
//
//    }

    "start the Application" in {
      Play.maybeApplication mustBe Some(app)
    }
  }
}
