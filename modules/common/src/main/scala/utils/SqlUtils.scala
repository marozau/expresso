package utils

import java.util
import java.util.Map

import com.google.common.base.{Joiner, Splitter}
import exceptions.BaseException
import org.postgresql.util.PSQLException
import slick.sql.SqlProfile.ColumnOption.SqlType

/**
  * @author im.
  */
object SqlUtils {

  val timestampTzNotNullType = SqlType("TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())")
  val timestampTzType = SqlType("TIMESTAMPTZ")


  private val propertiesSplitter: Splitter.MapSplitter = Splitter.on(',').withKeyValueSeparator('=')
  private val arrayJoiner: Joiner = Joiner.on(',')

  private val MESSAGE_START_INDEX: Int = 7
  private val ERROR_TAG: String = "<ERROR>"

  def parseProperties(e: PSQLException): util.Map[String, String] = {
    var properties: util.Map[String, String] = null
    val message: String = e.getMessage.substring(MESSAGE_START_INDEX)
    if (message.startsWith(ERROR_TAG)) {
      val beginIndex: Int = message.indexOf(ERROR_TAG) + ERROR_TAG.length
      val error: String = message.substring(beginIndex, message.indexOf(ERROR_TAG, beginIndex))
      properties = propertiesSplitter.split(error)
    }
    properties
  }

  @throws[BaseException]
  def parseException(e: PSQLException,
                     throwException: (String, () => String) => Unit): Unit = {
    val properties = parseProperties(e)
    throwException(properties.getOrDefault("code", ""), () => properties.getOrDefault("message", e.getMessage))
  }
}