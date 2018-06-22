import sbt._
import Keys._

/**
  * @author im.
  */
object Common {

  val xname = """expresso"""
  val xversion = "1.0"

  val settings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.12.6",
    organization := "today.expresso",
    resolvers := Resolvers.commonResolvers,
    version := xversion
  )
}
