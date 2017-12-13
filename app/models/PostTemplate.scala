package models

import java.net.URL

import clients.Compiler.HtmlTemplate
import play.api.Configuration

/**
  * @author im.
  */
case class PostTemplate(id: Option[Long],
                        title: String,
                        titleUrl: String,
                        annotation: String,
                        body: HtmlTemplate,
                        references: List[URL],
                        config: Configuration,
                        target: Target.Value)
