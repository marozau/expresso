package models.components

import models.Card
import slick.jdbc.GetResult
import today.expresso.common.db.Repository

trait CardComponent {
  this: Repository =>

  import api._

  implicit val getResultCard: GetResult[Card] = GetResult { r =>
    Card(
      r.nextLong(),
      r.nextLong(),
      r.nextString(),
      r.nextString(),
      r.nextString(),
      r.nextLocalDate(),
      r.nextString()
    )
  }
}
