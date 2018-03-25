package today.expresso.common.utils

import java.util.Properties

import play.api.Configuration

/**
  * @author im.
  */
object ConfigUtils {

  def getProperties(configuration: Configuration): Properties = {
    import scala.collection.JavaConverters._
    val props = new Properties()
    val map: Map[String, Object] = configuration.underlying.entrySet().asScala
      .map({ entry =>
        entry.getKey -> entry.getValue.unwrapped()
      })(collection.breakOut)
    props.putAll(map.asJava)
    props
  }
}
