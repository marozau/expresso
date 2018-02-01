package controllers

import javax.inject.{Inject, Singleton}

import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import services.auth.SignInService
import today.expresso.grpc.user.dto.UserDto

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class GraphQLApiController @Inject()(
                                      components: ControllerComponents,
                                      signInService: SignInService,
                                    )(
                                      implicit
                                      webJarsUtil: WebJarsUtil,
                                      assets: AssetsFinder,
                                      ex: ExecutionContext
                                    ) extends AbstractController(components) with I18nSupport {

  def view() = Action { implicit request =>
    Ok(views.html.signIn(
      forms.SignInForm.form,
      routes.GraphQLApiController.submit()))
  }

  def submit() = Action.async { implicit request =>
    forms.SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form, routes.GraphQLApiController.submit()))),
      data => {
        signInService.signIn(data.email, data.password, data.rememberMe)
          .map { case (user, token) =>
            if (user.roles.contains(UserDto.Role.API)) {
              Ok(views.html.graphiql(token)).withNewSession
            } else Unauthorized
          }
      }
    )
  }
}
