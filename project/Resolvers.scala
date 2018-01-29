import sbt._

object Resolvers {

  // Resolvers is needed for silhouette
  val atlassian = "Atlassian Releases" at "https://maven.atlassian.com/public/"
  val kamonRelease: MavenRepository = Resolver.bintrayRepo("kamon-io", "releases")
  val kamonSbt: URLRepository = Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")

  val commonResolvers = Seq(
    Opts.resolver.sonatypeSnapshots,
    atlassian,
    kamonRelease,
    Resolver.jcenterRepo
  )
}
