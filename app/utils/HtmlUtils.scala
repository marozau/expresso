package utils

import java.net.URL
import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import play.api.data.FormError
import services.Compiler
import services.Helper.CompilationError

/**
  * @author im.
  */
object HtmlUtils {

  import play.api.data.format.Formats._
  import play.api.data.format.Formatter
  implicit object UrlFormatter extends Formatter[URL] {
    override val format = Some(("format.url", Nil))
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], URL] =
      parsing(new URL(_), "error.url", Nil)(key, data)
    override def unbind(key: String, value: URL) = Map(key -> value.toString)
  }

}

@Singleton
class HtmlUtils @Inject()(compiler: Compiler) {

  import play.api.data.validation.Constraint
  import play.api.data.validation.{Valid, Invalid, ValidationError}
  import Compiler._

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
