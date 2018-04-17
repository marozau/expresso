package models

import java.time.LocalDate

case class Card(paymentMethodId: Long,
                userId: Long,
                /**
                  * NOT ALLOWED to log or store this value
                  */
                pan: String,
                encryptedPan: String,
                token: String,
                expirationDate: LocalDate,
                cardholderName: String)
