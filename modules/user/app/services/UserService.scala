package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import exceptions.InvalidEmailException
import models.User
import models.daos.UserDao

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class UserService @Inject()(userDao: UserDao,
                            passwordHasherRegistry: PasswordHasherRegistry,
                            authTokenService: AuthTokenService)(implicit ec: ExecutionContext) {

  def getById(userId: Long): Future[Option[User]] = userDao.getById(userId)

  //TODO: validate email address
  //TODO: validate password
  def save(email: String, password: String, locale: Option[String], timezone: Option[Int]): Future[User] = {
    val domain = "expresso.today"
    if (!email.endsWith(domain))
      return Future.failed(InvalidEmailException(s"$email is invalid, @expresso.today domain is allowed only"))

    val authInfo = passwordHasherRegistry.current.hash(password)
    userDao.create(email, authInfo.password, authInfo.hasher, authInfo.salt, locale, timezone)
      .flatMap { user =>
        import scala.concurrent.duration._
        authTokenService.create(user.id, 1.day).map { token =>
          //          val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
          //TODO: replace by emailService request
          //TODO: send async using message bus, user should not wait for
          //TODO: resent email when we fail to send it first time and client try to login with NEW status
          val url = s"http://localhost:9000/activate?token=${token.id}"
          //          mailerClient.send(Email(
          //            subject = "email.sign.up.subject",
          //            from = "email.from",
          //            to = Seq(request.email),
          //            bodyText = Some(s"""Click <a href="$url">here</a> to send the activation email again.""")
          //            //                bodyText = Some(views.txt.emails.signUp(user, url).body),
          //            //                bodyHtml = Some(views.html.emails.signUp(user, url).body)
          //          ))
          user
        }
      }
  }

  def verify(userId: Long, token: UUID): Future[User] = {
    userDao.verify(userId, token)
  }

  def createReader(email: String, locale: Option[String]) = {
    userDao.createReader(email, locale)
  }
}
