package models

import java.time.LocalDate

/**
  * @author im.
  */
case class EditionSpec(id: Long,
                        date: LocalDate,
                        title: Option[String])
