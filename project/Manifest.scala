import sbt._
import Keys._
import java.util.Date

object Manifest {

  lazy val manifestSettings = Seq(
    packageOptions in (Compile, packageBin) +=
      Package.ManifestAttributes( "Build" -> new Date().toString )
  )
}