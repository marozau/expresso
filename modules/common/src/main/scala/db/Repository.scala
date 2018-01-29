package db

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * @author im.
  */
trait Repository extends PostgresDriver {
  protected val dbConfig: DatabaseConfig[JdbcProfile]
}
