package models

import clients.Compiler.HtmlTemplate
import play.api.Configuration

/**
  * @author im.
  */
case class PostTemplate(id: Long,
                        title: String,
                        titleUrl: String,
                        annotation: String,
                        body: HtmlTemplate,
                        config: Configuration,
                        target: Target.Value)
