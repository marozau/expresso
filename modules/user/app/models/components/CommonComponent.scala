package models.components

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import db.Repository
import slick.jdbc.{GetResult, SetParameter}

import scala.concurrent.duration.FiniteDuration

/**
  * @author im.
  */
trait CommonComponent {
  this: Repository =>

  import api._

  // PARAMETERS

  implicit val setParameterUUID: SetParameter[UUID] = PlainSQLUtils.mkSetParameter[UUID]("uuid")
  implicit val setParameterFiniteDuration: SetParameter[FiniteDuration] = SetParameter { (t, pp) => pp.setLong(t.toMillis) }


  // RESULTS

  implicit val getResultUnit: GetResult[Unit] = GetResult { _ => Unit }


}
