package models.components

import java.util.UUID

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import slick.jdbc.{GetResult, SetParameter}
import today.expresso.common.db.Repository

import scala.concurrent.duration.FiniteDuration

/**
  * @author im.
  */
trait CommonComponent {
  this: Repository =>

  // PARAMETERS

  implicit val setParameterUUID: SetParameter[UUID] = PlainSQLUtils.mkSetParameter[UUID]("uuid")
  implicit val setParameterFiniteDuration: SetParameter[FiniteDuration] = SetParameter { (t, pp) => pp.setLong(t.toMillis) }


  // RESULTS

  implicit val getResultUnit: GetResult[Unit] = GetResult { _ => Unit }


}
