package today.expresso.templates.api

import play.api.Configuration
import play.twirl.api.Html

import scala.concurrent.Future

/**
  * @author im.
  */
object HtmlCompiler {
  type HtmlTemplate = (Configuration) => Html
}

trait HtmlCompiler {
  import HtmlCompiler._

  def compile(html: String, packageName: Option[String] = None, imports: Seq[String] = List.empty): HtmlTemplate
  def compileAsync(html: String, packageName: Option[String] = None, imports: Seq[String] = List.empty): Future[HtmlTemplate]
}
