package controllers

import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import controllers.dto.LoginDto
import models.ApplicationContext
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.auth.SignInService
import today.expresso.grpc.Header
import today.expresso.grpc.user.service.{UserCreateRequest, UserServiceGrpc}

import scala.concurrent.ExecutionContext

/**
  * The `Sign In` controller.
  *
  * @param components          The Play controller components.
  * @param webJarsUtil         The webjar util.
  * @param assets              The Play assets finder.
  */
@Singleton
class SignInController @Inject()(
                                  app: ApplicationContext,
                                  components: ControllerComponents,
                                  userServiceStub: UserServiceGrpc.UserService,
                                  signInService: SignInService,
                                )(
                                  implicit
                                  webJarsUtil: WebJarsUtil,
                                  assets: AssetsFinder,
                                  ex: ExecutionContext
                                ) extends AbstractController(components) with I18nSupport {


  def signIn = app.auth.silhouette.UnsecuredAction.async(parse.json) { implicit request =>
    import play.api.libs.json.Json
    val login = request.body.as[LoginDto]
    signInService.signIn(login.email, login.password, remember = true)
      .map { case (user, token) =>
        Ok(Json.obj("token" -> token))
      }
  }

  def signUp(email: String, password: String) = app.auth.silhouette.UnsecuredAction.async { implicit request =>
    userServiceStub.userCreate(
      UserCreateRequest(
        Some(Header(ThreadLocalRandom.current().nextInt())),
        email,
        password
      )
    )
      .map(_.user)
      .map(user => Ok(Json.obj("user" -> user.toString)))
  }
}
