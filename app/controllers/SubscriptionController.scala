package controllers

import javax.inject.{Inject, Singleton}

import org.apache.commons.lang3.exception.ExceptionContext
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * @author im.
  */
@Singleton
class SubscriptionController @Inject()(cc: ControllerComponents)(implicit ec: ExceptionContext)
  extends AbstractController(cc) with I18nSupport {


  def subscribe(email: String) = {

  }

  def unsubscirbe(email: String) = {

  }
}
