package models.components

import java.util.UUID

import com.github.tminglei.slickpg.utils.PlainSQLUtils
import today.expresso.common.db.Repository
import slick.jdbc.{GetResult, JdbcType, SetParameter}
import today.expresso.stream.domain.model.newsletter.Locale

import scala.concurrent.duration.FiniteDuration

/**
  * @author im.
  */
trait CommonComponent {
  this: Repository =>

  // PARAMETERS

  implicit val setParameterUUID: SetParameter[UUID] = PlainSQLUtils.mkSetParameter[UUID]("uuid")
  implicit val setParameterFiniteDuration: SetParameter[FiniteDuration] = SetParameter { (t, pp) => pp.setLong(t.toMillis) }

  implicit val localeMapper: JdbcType[Locale.Value] = createEnumJdbcType("LOCALE", Locale)
  implicit val localeOptionMapper = createEnumOptionColumnExtensionMethodsBuilder(Locale)
  implicit val localeListMapper: JdbcType[List[Locale.Value]] = createEnumListJdbcType("LOCALE", Locale)

  implicit val localeSetParameter: SetParameter[Locale.Value] = PlainSQLUtils.mkSetParameter[Locale.Value]("LOCALE")
  implicit val localeOptionSetParameter: SetParameter[Option[Locale.Value]] = PlainSQLUtils.mkOptionSetParameter[Locale.Value]("LOCALE")

  // RESULTS

  implicit val getResultUnit: GetResult[Unit] = GetResult { _ => Unit }


}
