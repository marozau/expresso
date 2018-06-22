package models.components

import java.net.URL

import today.expresso.common.db.Repository
import play.api.i18n.Lang
import slick.jdbc.GetResult
import today.expresso.stream.domain.model.newsletter.Newsletter

/**
  * @author im.
  */
trait NewsletterComponent extends CommonComponent {
  this: Repository =>

  import api._

  implicit val newsletterGetResult: GetResult[Newsletter] = GetResult { r =>
    Newsletter(
      r.nextLong(),
      r.nextLong(),
      r.nextString(),
      localeMapper.getValue(r.rs, r.skip.currentPos),
      r.nextStringOption().map(new URL(_)),
      r.nextStringOption().map(new URL(_)),
      Option(playJsonTypeMapper.getValue(r.rs, r.skip.currentPos)),
      r.nextTimestamp().toInstant
    )
  }
}
