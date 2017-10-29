package forms.auth

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

/**
  * The form which handles the sign up process.
  */
object SignUpForm {

  /**
    * Allowed doamin
    */
  final val domain = "expresso.today"
  /**
    * Form constraint that check that sign up form email is from expresso.today domain
    */
  val emailDomainConstraint: Constraint[String] = Constraint("constraints.email.domain")({
    email =>
      if (email.endsWith(domain)) Valid
      else Invalid(Seq(ValidationError("sign.up.email.invalid.domain")))
  })

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "email" -> email.verifying(emailDomainConstraint),
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param email The email of the user.
    */
  case class Data(email: String, password: String)
}
