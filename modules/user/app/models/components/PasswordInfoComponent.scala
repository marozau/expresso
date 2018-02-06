package models.components

import com.mohiva.play.silhouette.api.util.PasswordInfo
import db.Repository
import slick.jdbc.GetResult

/**
  * @author im.
  */
trait PasswordInfoComponent {
  this: Repository =>

  implicit val getResultPasswordInfo: GetResult[PasswordInfo] = GetResult { r =>
    r.skip // skip login_info_id
    PasswordInfo(
      r.nextString(),
      r.nextString(),
      r.nextStringOption()
    )
  }
}
