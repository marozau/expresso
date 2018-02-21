package today.expresso.compiler.api.domain

/**
  * @author im.
  */
object Target extends Enumeration {
  type Target = Value
  val DEV, EMAIL, TELEGRAM, VIBER, SITE, LANDING, ANALYTICS = Value
}