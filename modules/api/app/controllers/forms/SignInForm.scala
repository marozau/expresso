package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

/**
  * @author im.
  */
object SignInForm {

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param email      The email of the user.
    * @param password   The password of the user.
    * @param rememberMe Indicates if the user should stay logged in on the next visit.
    */
  case class Data(
                   email: String,
                   password: String,
                   rememberMe: Boolean)

}

