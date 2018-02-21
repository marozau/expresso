package utils

import java.util
import java.util.Collections

import com.google.common.base.Splitter
import exceptions.BaseException
import org.postgresql.util.PSQLException
import slick.sql.SqlProfile.ColumnOption.SqlType

import scala.util.{Failure, Success, Try}

/**
  * @author im.
  */
object SqlUtils {

  val timestampTzNotNullType = SqlType("TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())")
  val timestampTzType = SqlType("TIMESTAMPTZ")

  private val propertiesSplitter: Splitter.MapSplitter = Splitter.on(',').withKeyValueSeparator('=')

  private val MESSAGE_START_INDEX: Int = 7
  private val ERROR_TAG: String = "<ERROR>"

  def parseProperties(e: PSQLException): util.Map[String, String] = {
    val message: String = e.getMessage.substring(MESSAGE_START_INDEX)
    if (message.startsWith(ERROR_TAG)) {
      val beginIndex: Int = message.indexOf(ERROR_TAG) + ERROR_TAG.length
      val error: String = message.substring(beginIndex, message.indexOf(ERROR_TAG, beginIndex))
      propertiesSplitter.split(error)
    } else {
      Collections.emptyMap[String, String]
    }
  }

  @throws[BaseException]
  def parseException(e: PSQLException,
                     throwException: (String, () => String) => Unit): Unit = {
    val properties = parseProperties(e)
    throwException(properties.getOrDefault("code", ""), () => properties.getOrDefault("message", e.getMessage))
  }

  def tryException[T](throwExceptions: ((String, () => String) => Unit)*): Try[T] => T = {
    case Success(res) => res
    case Failure(e: PSQLException) =>
      throwExceptions.foreach(SqlUtils.parseException(e, _))
      throw e
    case Failure(e: Throwable) => throw e
  }
}