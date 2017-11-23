name := """expresso"""
organization := "today.expresso"

version := "1.0"

// Resolver is needed only for SNAPSHOT versions
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/models.repositories/snapshots/"
// Resolvers is needed for silhouette
resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value


lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(Manifest.manifestSettings: _*)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += filters
libraryDependencies += ws
libraryDependencies += cacheApi

libraryDependencies += "com.typesafe.play" %% "twirl-compiler" % "1.3.3"
libraryDependencies += "com.typesafe.play" %% "twirl-api" % "1.3.3"
libraryDependencies += "com.typesafe.play" %% "twirl-parser" % "1.3.3"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.2",
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.github.tminglei" %% "slick-pg" % "0.15.4",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.15.4",
  "com.google.cloud.sql" % "postgres-socket-factory" % "1.0.4"
)
libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.3.0"

libraryDependencies += "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.7"

libraryDependencies += "com.viber" % "viber-bot" % "1.0.10"

libraryDependencies += "info.mukel" %% "telegrambot4s" % "2.9.5"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test

val elastic4sVersion = "5.4.6"
libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-streams" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-play-json" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion % "test"
)

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"

libraryDependencies += "org.reflections" % "reflections" % "0.9.11"

libraryDependencies ++= Seq(
//  "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery"),
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" %% "webjars-play" % "2.6.1",
//  "org.webjars" % "jquery" % "3.2.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "expresso.today.controllers._"

//https://www.playframework.com/documentation/2.5.x/ScalaRequestBinders
// Adds additional packages into conf/routes
//play.sbt.routes.RoutesKeys.routesImport += "util.Binders._"
//play.sbt.routes.RoutesKeys.routesImport += "binders.ReadyPostBinder._"
//play.sbt.routes.RoutesKeys.routesImport += "models._"
//play.sbt.routes.RoutesKeys.routesImport += "clients.PublishingHouse._"

//https://github.com/KarelCemus/play-redis
libraryDependencies += "com.github.karelcemus" %% "play-redis" % "1.6.1"
//https://github.com/cb372/scalacache
libraryDependencies += "com.github.cb372" %% "scalacache-caffeine" % "0.10.0"
//https://github.com/mohiva/play-html-compressor/blob/master/README.md
libraryDependencies += "com.mohiva" %% "play-html-compressor" % "0.7.1"

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "com.iheart" %% "ficus" % "1.4.3"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.2",
  "com.mohiva" %% "play-silhouette-cas" % "5.0.2",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.2",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.2",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.2",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.2" % "test"
)

libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.0"

libraryDependencies += "com.google.cloud" % "google-cloud-logging-logback" % "0.30.0-alpha"
