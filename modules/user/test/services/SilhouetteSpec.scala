package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import config.TestContext
import today.expresso.common.exceptions.UserNotFoundException
import models.{ApplicationContext, User}
import today.expresso.common.utils.HashUtils

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author im.
  */
class SilhouetteSpec extends TestContext {

  val ctx = app.injector.instanceOf[ApplicationContext]
  val userService = app.injector.instanceOf[UserService]
  val passwordHasherRegistry = app.injector.instanceOf[PasswordHasherRegistry]

  val email = "test@expresso.today"
  val pass = "test"
  var userId: Long = _
  var loginInfo: LoginInfo = _
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    userId = Await.result(userService.save(email, pass, None, None), 5.seconds).id
    val creds = Credentials(HashUtils.encode(email), pass)
    loginInfo = Await.result(ctx.auth.credentialsProvider.authenticate(creds), 5.seconds)
  }

  "Test Silhuoette" must {

    "authenticate user" in {
      val creds = Credentials(HashUtils.encode(email), pass)
      whenReady(ctx.auth.credentialsProvider.authenticate(creds)){ res =>
        res mustBe a [LoginInfo]
      }
    }

    "not authenticate user with wrong password" in {
      val creds = Credentials(HashUtils.encode(email), "brutforce:)")
      whenReady(ctx.auth.credentialsProvider.authenticate(creds).failed){ res =>
        res mustBe an [InvalidPasswordException]
      }
    }

    "not authenticate user with wrong email" in {
      val creds = Credentials(HashUtils.encode("test2@gmail.com"), pass)
      whenReady(ctx.auth.credentialsProvider.authenticate(creds).failed){ res =>
        res mustBe an [IdentityNotFoundException]
      }
    }

    "get user by login info" in {
      whenReady(ctx.auth.silhouette.env.identityService.retrieve(loginInfo)) { user =>
        user.get.id mustBe userId
      }
    }

    "change password" in {
      val passwordInfo = passwordHasherRegistry.current.hash("newPassword")
      whenReady(ctx.auth.authInfoRepository.update(loginInfo, passwordInfo)) { res =>
        res mustBe an [PasswordInfo]
      }
      val creds = Credentials(HashUtils.encode(email), pass)
      whenReady(ctx.auth.credentialsProvider.authenticate(creds).failed){ res =>
        res mustBe an [InvalidPasswordException]
      }

      val newCreds = Credentials(HashUtils.encode(email), "newPassword")
      whenReady(ctx.auth.credentialsProvider.authenticate(newCreds)){ res =>
        res mustBe a [LoginInfo]
      }
    }

    "don't change for uknown user" in {
      val passwordInfo = passwordHasherRegistry.current.hash("newPassword")
      whenReady(ctx.auth.authInfoRepository.update(LoginInfo("credentials", "bruteforce:)"), passwordInfo).failed) { res =>
        res mustBe an [UserNotFoundException]
      }
    }

    "change only single user password" in {
      val email0 = "test0@expresso.today"
      whenReady(userService.save(email0, pass, None, None)) {res =>
        res mustBe a [User]
      }
      val passwordInfo = passwordHasherRegistry.current.hash("newPassword")
      whenReady(ctx.auth.authInfoRepository.update(loginInfo, passwordInfo)) { res =>
        res mustBe an [PasswordInfo]
      }
      val creds = Credentials(HashUtils.encode(email), "newPassword")
      whenReady(ctx.auth.credentialsProvider.authenticate(creds)){ res =>
        res mustBe a [LoginInfo]
      }
      val creds0 = Credentials(HashUtils.encode(email0), pass)
      whenReady(ctx.auth.credentialsProvider.authenticate(creds0)){ res =>
        res mustBe a [LoginInfo]
      }
    }

    "update password using save" in {
      val passwordInfo = passwordHasherRegistry.current.hash("newPassword")
      whenReady(ctx.auth.authInfoRepository.save(loginInfo, passwordInfo)) { res =>
        res mustBe an [PasswordInfo]
      }
      val newCreds = Credentials(HashUtils.encode(email), "newPassword")
      whenReady(ctx.auth.credentialsProvider.authenticate(newCreds)){ res =>
        res mustBe a [LoginInfo]
      }
    }

    "remove password and then return back using save" in {
      import scala.reflect._
      whenReady(ctx.auth.authInfoRepository.remove(loginInfo)(classTag[PasswordInfo])) { res =>
        res mustBe an [scala.runtime.BoxedUnit]
      }
      val creds = Credentials(HashUtils.encode(email), pass)
      whenReady(ctx.auth.credentialsProvider.authenticate(creds).failed){ res =>
        res mustBe an [IdentityNotFoundException]
      }

      val passwordInfo = passwordHasherRegistry.current.hash(pass)
      whenReady(ctx.auth.authInfoRepository.save(loginInfo, passwordInfo)) { res =>
        res mustBe an [PasswordInfo]
      }
      whenReady(ctx.auth.credentialsProvider.authenticate(creds)){ res =>
        res mustBe a [LoginInfo]
      }
    }

    "get password info" in {
      import scala.reflect._
      whenReady(ctx.auth.authInfoRepository.find(loginInfo)(classTag[PasswordInfo])) { res =>
        res.get mustBe a [PasswordInfo]
      }
    }
  }
}
