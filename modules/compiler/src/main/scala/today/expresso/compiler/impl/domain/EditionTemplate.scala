package today.expresso.compiler.impl.domain

import java.time.LocalDate

import play.api.Configuration
import today.expresso.compiler.api.HtmlCompiler.HtmlTemplate
import today.expresso.compiler.api.domain.{Newsletter, Target}

/**
  * @author im.
  */
case class EditionTemplate(id: Long,
                           url: String,
                           newsletter: Newsletter,
                           date: LocalDate,
                           title: String,
                           posts: List[PostTemplate],
                           header: Option[HtmlTemplate],
                           footer: Option[HtmlTemplate],
                           config: Configuration,
                           target: Target.Value)
