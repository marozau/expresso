package exceptions

/**
  * @author im.
  */
case class EmailSendException(message: String, cause: Throwable = null) extends Exception(message, cause)
