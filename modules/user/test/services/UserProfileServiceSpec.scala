package services

import config.TestContext
import today.expresso.common.exceptions.UserNotFoundException

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author im.
  */
class UserProfileServiceSpec extends TestContext {

  val userService = app.injector.instanceOf[UserService]
  val userProfileService = app.injector.instanceOf[UserProfileService]

  val email = "test@expresso.today"
  val pass = "test"
  var userId: Long = _
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    userId = Await.result(userService.save(email, pass, None, None), 5.seconds).id
  }

  "UserProfile service" must {

    "get user profile by id" in {
      whenReady(userProfileService.getByUserId(userId)){ res =>
        res.userId mustBe userId
      }

      whenReady(userProfileService.getByUserId(0).failed){ res =>
        res mustBe an [UserNotFoundException]
      }
    }
  }

}
