package models.components

import models.Currency
import slick.jdbc.GetResult
import today.expresso.common.db.Repository

trait CurrencyComponent {
  this: Repository =>

  implicit val currencyGetResult: GetResult[Currency] = GetResult { r =>
    Currency(
      r.nextString(),
      r.nextInt(),
      r.nextString(),
      r.nextString(),
      r.nextInt(),
      r.nextBoolean()
    )
  }
}
