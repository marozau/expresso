package models

import java.net.URL
import java.time.LocalDate

import clients.Compiler.HtmlTemplate
import play.api.Configuration

/**
  * @author im.
  */
case class EditionTemplate(id: Option[Long],
                           newsletter: Newsletter,
                           date: LocalDate,
                           url: Option[URL],
                           title: Option[String],
                           header: Option[HtmlTemplate],
                           footer: Option[HtmlTemplate],
                           posts: List[PostTemplate],
                           config: Configuration,
                           target: Target.Value)
