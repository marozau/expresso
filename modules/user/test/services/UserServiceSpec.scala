package services

import config.TestContext
import exceptions.{InvalidAuthTokenException, InvalidEmailException}
import models.User

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author im.
  */
class UserServiceSpec extends TestContext {

  val userService = app.injector.instanceOf[UserService]
  val authTokenService = app.injector.instanceOf[AuthTokenService]

  "Test UserService" must {

    val email = "test@expresso.today"
    val password = "test"

    "create user" in {
      whenReady(userService.save(email, password, Some("en"), Some(1))) { res =>
        res.email mustBe email
        res.status mustBe User.Status.NEW
        res.roles mustBe List(User.Role.USER)
        res.locale mustBe Some("en")
        res.timezone mustBe Some(1)
        res.reason mustBe None
      }
    }

    "reject user with domain other than @expresso.today" in {
      whenReady(userService.save("test@test.com", "test", None, None).failed) { t =>
        t mustBe an[InvalidEmailException]
      }
    }

    "get user by id" in {
      whenReady(
        userService.save(email, password, Some("en"), Some(1))
          .flatMap { user =>
            userService.getById(user.id).map((user, _))
          }
      ) { case (user, res: Option[User]) =>
        res.nonEmpty mustBe true
        res.get mustBe user
      }
    }

    "verify user by token" in {
      val userAndTokenFuture = userService.save(email, password, None, None).flatMap { user =>
        authTokenService.create(user.id).map((user, _))
      }
      whenReady(userAndTokenFuture.flatMap { case (user, token) =>
        userService.verify(user.id, token.id)
      }) { res =>
        res.status mustBe User.Status.VERIFIED
      }

      whenReady(userAndTokenFuture.flatMap { case (user, token) =>
        userService.verify(user.id, token.id)
      }.failed) { t =>
        t mustBe an[InvalidAuthTokenException]
      }
    }

    "create reader" in {
      val testEmail = "test@test.com"
      whenReady(userService.createReader(testEmail, None)) {user =>
        user.email mustBe testEmail
        user.roles mustBe List(User.Role.READER)
      }
    }

    "add READER role to the already created user" in {
      whenReady(userService.save(email, password, Some("en"), Some(1))) { user =>
        user.roles mustBe List(User.Role.USER)
      }
      whenReady(userService.createReader(email, None)) {user =>
        user.email mustBe email
        user.roles mustBe List(User.Role.USER, User.Role.READER)
      }
    }
  }
}
