package today.expresso.templates.impl.domain

import play.api.Configuration
import today.expresso.templates.api.HtmlCompiler.HtmlTemplate
import today.expresso.templates.api.domain.Target

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
