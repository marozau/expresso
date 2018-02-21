package today.expresso.compiler.impl.domain

import play.api.Configuration
import today.expresso.compiler.api.HtmlCompiler.HtmlTemplate
import today.expresso.compiler.api.domain.Target

/**
  * @author im.
  */
case class PostTemplate(id: Long,
                        url: String,
                        title: String,
                        annotation: String,
                        body: HtmlTemplate,
                        config: Configuration,
                        target: Target.Value)
