package controllers

import javax.inject.{Inject, Singleton}

import models.User
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

  def view(role: String) = Action { implicit request =>
    Ok(views.html.signIn(
      forms.SignInForm.form,
      routes.GraphQLApiController.submit(role)))
  }

  def submit(role: String) = Action.async { implicit request =>
    forms.SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form, routes.GraphQLApiController.submit(role)))),
      data => {
        signInService.signIn(data.email, data.password, data.rememberMe)
          .map { case (user, token) =>
            if (role.nonEmpty && !user.roles.contains(User.strToRoleMapper(role.toUpperCase))) {
              Unauthorized
            } else {
              Ok(views.html.graphiql(token, role)).withNewSession
            }
          }
      }
    )
  }
}
