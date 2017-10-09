package utils

import slick.sql.SqlProfile.ColumnOption.SqlType

/**
  * @author im.
  */
object SqlUtils {

  val timestampTzNotNullType = SqlType("TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())")
  val timestampTzType = SqlType("TIMESTAMPTZ")

}
