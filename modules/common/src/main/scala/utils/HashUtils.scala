package utils

import java.nio.charset.Charset

import com.google.common.hash.Hashing

/**
  * @author im.
  */
object HashUtils {

  def encode(string: String) = Hashing.sha256().hashString(string, Charset.forName("UTF-8"))
}
