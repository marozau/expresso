package models

case class Currency(code: String,
                    numericCode: Int,
                    symbol: String,
                    name: String,
                    unit: Int,
                    accountCurrency: Boolean)
