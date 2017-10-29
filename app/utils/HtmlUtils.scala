package utils

import javax.inject.{Inject, Singleton}

import clients.Compiler
import clients.Helper.CompilationError
import play.api.Configuration

/**
  * @author im.
  */
object HtmlUtils {
}

@Singleton
class HtmlUtils @Inject()(compiler: Compiler) {

  import Compiler._
  import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

  val htmlCheckConstraint: Constraint[String] = Constraint("constraints.htmlcheck")({
    plainText =>
      if (plainText == null || plainText.isEmpty) {
        Invalid(Seq(ValidationError("null or empty html body is not allowed")))
      } else {
        try {
          // replace views.html.email with specific view for validation
          compiler.compileBlocking(header + plainText, Some("views.html.email"))(Configuration.empty)
          Valid
        } catch {
          case t: CompilationError =>
            Invalid(Seq(ValidationError(s"${t.line - headerLines + 1}:${t.column} - ${t.message}")))
          case t: Throwable =>
            Invalid(Seq(ValidationError(s"unknown error: ${t.getMessage}")))
        }
      }
  })
}
