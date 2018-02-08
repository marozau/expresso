package services

import java.time.Instant

import config.TestContext
import exceptions.InvalidAuthTokenException
import models.AuthToken

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author im.
  */
class AuthTokenServiceSpec extends TestContext {

  val authTokenService = app.injector.instanceOf[AuthTokenService]
  val userService = app.injector.instanceOf[UserService]

  val jitterSec: Long = 1.second.toSeconds
  val email = "test@expresso.today"
  val pass = "test"
  var userId: Long = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    userId = Await.result(userService.save(email, pass, None, None), 5.seconds).id
  }

  "AuthToken service" must {

    "create token for user" in {
      whenReady(authTokenService.create(userId)) { res =>
        res.expiry must be >= Instant.now().plusSeconds(5.seconds.toSeconds - jitterSec)
      }
      whenReady(authTokenService.create(userId, 1.day)) { res =>
        res.expiry must be >= Instant.now().plusSeconds(1.day.toSeconds - jitterSec)
      }
      whenReady(authTokenService.create(userId, 7.days)) { res =>
        res.expiry must be >= Instant.now().plusSeconds(7.days.toSeconds - jitterSec)
      }
    }

    "validate token" in {
      val token = Await.result(authTokenService.create(userId), 5.seconds)

      whenReady(authTokenService.validate(token.id)) { res =>
        res.get mustBe an[AuthToken]
      }

      whenReady(authTokenService.validate(token.id).failed) { res =>
        res mustBe an[InvalidAuthTokenException]
      }
    }

    "expire token" in {
      val token = Await.result(authTokenService.create(userId, 300.milli), 5.seconds)

      eventually {
        whenReady(authTokenService.validate(token.id).failed) { res =>
          res mustBe an[InvalidAuthTokenException]
        }
      }
    }
  }
}
