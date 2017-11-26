package utils

import slick.sql.SqlProfile.ColumnOption.SqlType

/**
  * @author im.
  */
object SqlUtils {

  val timestampTzNotNullType = SqlType("TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())")
  val timestampTzType = SqlType("TIMESTAMPTZ")

  object PostgreSQLErrorCodes extends Enumeration {
    type PostgreSQLErrorCodes = Value
    val UniqueViolation = Value("23505")
  }
}