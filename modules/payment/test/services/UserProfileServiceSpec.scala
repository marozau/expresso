package services

import config.TestContext
import models.UserProfile
import today.expresso.common.exceptions.UserNotFoundException

/**
  * @author im.
  */
class UserProfileServiceSpec extends TestContext {

  val userProfileService = app.injector.instanceOf[UserProfileService]

  val pass = "test"
  var userId: Long = _
  override protected def beforeEach(): Unit = {
    super.beforeEach()
  }

  "UserProfile service" must {

    "update and get user profile" in {

      whenReady(userProfileService.update(1, Some(UserProfile.Status.VERIFIED), None, None, None, None, None, None)) { res =>
        res.userId mustBe userId
      }

      whenReady(userProfileService.getByUserId(userId)){ res =>
        res.userId mustBe userId
      }

      whenReady(userProfileService.getByUserId(0).failed){ res =>
        res mustBe an [UserNotFoundException]
      }
    }
  }

}
