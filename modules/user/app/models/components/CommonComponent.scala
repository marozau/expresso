package models.components

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

import db.Repository
import slick.jdbc.{GetResult, SetParameter}

/**
  * @author im.
  */
trait CommonComponent {
  this: Repository =>

  import api._

  // PARAMETERS

  implicit val setParameterLongList: SetParameter[List[Long]] = SetParameter { (t, pp) => simpleLongListTypeMapper.setValue(t, pp.ps, pp.pos + 1) }
  implicit val setParameterUUID: SetParameter[UUID] = SetParameter { (t, pp) => uuidColumnType.setValue(t, pp.ps, pp.pos + 1) }
  implicit val setParameterInstant: SetParameter[Instant] = SetParameter { (t, pp) => pp.setTimestamp(Timestamp.from(t)) }


  // RESULTS

  implicit val getResultUnit: GetResult[Unit] = GetResult { _ => Unit }


}
