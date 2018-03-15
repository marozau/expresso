import sbt._
import Keys._

/**
  * @author im.
  */
object Dependency {

  object v {
    lazy val play = "2.6.11"

    lazy val twirl = "1.3.12"
    lazy val slick = "3.0.3"
    lazy val postgresql = "42.1.4"
    lazy val slickPg = "0.16.0"
    lazy val gcpPostgresSocketFactory = "1.0.4"
    lazy val quartz = "2.3.0"
    lazy val viber = "1.0.10"
    lazy val telegrambot4s = "2.9.5"
    lazy val elastic4s = "5.4.6"
    lazy val playMailer = "6.0.1"
    lazy val reflections = "0.9.11"
    lazy val bootstrap = "3.3.7-1"
    lazy val bootstrapPlay = "1.2-P26-B3"
    lazy val webjars = "2.6.2"
    lazy val redisPlay = "2.0.2"
    lazy val scalacache = "0.10.0"
    lazy val playHtmlCompressor = "0.7.1"
    lazy val scalaGuice = "4.1.0"
    lazy val ficus = "1.4.3"
    lazy val silhouette = "5.0.2"
    lazy val avro4s = "1.8.3"
    lazy val sangria = "1.3.3"
    lazy val sangriaPlayJson = "1.0.4"
    lazy val sangriaSlowlog = "0.1.4"
    lazy val grpc = "1.8.0"
    lazy val scalapbGrpc = "0.6.7"
    lazy val flyway = "4.0.0"
    lazy val akka = "2.5.8"
    lazy val kafka = "1.0.1"
    lazy val confluent = "4.0.0"

    // Tests
    lazy val scalatestplus = "3.1.2"
    lazy val mokito = "2.13.0"
  }

  lazy val play: ModuleID = "com.typesafe.play" %% "play" % v.play

  lazy val twirlCompiler: ModuleID = "com.typesafe.play" %% "twirl-compiler" % v.twirl
  lazy val twirlApi: ModuleID = "com.typesafe.play" %% "twirl-api" % v.twirl
  lazy val twirlParser: ModuleID = "com.typesafe.play" %% "twirl-parser" % v.twirl

  lazy val postgresql: ModuleID = "org.postgresql" % "postgresql" % v.postgresql
  lazy val slickPlay: ModuleID = "com.typesafe.play" %% "play-slick" % v.slick
  lazy val slickPlayEvolutions: ModuleID = "com.typesafe.play" %% "play-slick-evolutions" % v.slick
  lazy val slickPg: ModuleID = "com.github.tminglei" %% "slick-pg" % v.slickPg
  lazy val slickPgPlayJson: ModuleID = "com.github.tminglei" %% "slick-pg_play-json" % v.slickPg
  lazy val gcpPostgresSocketFactory: ModuleID = "com.google.cloud.sql" % "postgres-socket-factory" % v.gcpPostgresSocketFactory

  lazy val quartz: ModuleID = "org.quartz-scheduler" % "quartz" % v.quartz

  lazy val viber: ModuleID = "com.viber" % "viber-bot" % v.viber
  lazy val telegrambot4s: ModuleID = "info.mukel" %% "telegrambot4s" % v.telegrambot4s

  lazy val elastic4sCore: ModuleID = "com.sksamuel.elastic4s" %% "elastic4s-core" % v.elastic4s
  lazy val elastic4sHttp: ModuleID = "com.sksamuel.elastic4s" %% "elastic4s-http" % v.elastic4s
  lazy val elastic4sStreams: ModuleID = "com.sksamuel.elastic4s" %% "elastic4s-streams" % v.elastic4s
  lazy val elastic4sPlayJson: ModuleID = "com.sksamuel.elastic4s" %% "elastic4s-play-json" % v.elastic4s
  // Tests
  lazy val elastic4sTestKit: ModuleID = "com.sksamuel.elastic4s" %% "elastic4s-testkit" % v.elastic4s % Test
  lazy val elastic4sEmbedded: ModuleID = "com.sksamuel.elastic4s" %% "elastic4s-embedded" % v.elastic4s % Test

  lazy val playMailer: ModuleID = "com.typesafe.play" %% "play-mailer" % v.playMailer
  lazy val playMailerGuice: ModuleID = "com.typesafe.play" %% "play-mailer-guice" % v.playMailer

  lazy val reflections: ModuleID = "org.reflections" % "reflections" % v.reflections

  lazy val bootstrap: ModuleID = "org.webjars" % "bootstrap" % v.bootstrap
  lazy val webjars: ModuleID = "org.webjars" %% "webjars-play" % v.webjars
  lazy val bootstrapPlay: ModuleID = "com.adrianhurt" %% "play-bootstrap" % v.bootstrapPlay

  lazy val redisPlay: ModuleID = "com.github.karelcemus" %% "play-redis" % v.redisPlay
  lazy val scalacache: ModuleID = "com.github.cb372" %% "scalacache-caffeine" % v.scalacache

  lazy val playHtmlCompressor: ModuleID = "com.mohiva" %% "play-html-compressor" % v.playHtmlCompressor

  lazy val scalaGuice: ModuleID = "net.codingwell" %% "scala-guice" % v.scalaGuice
  lazy val ficus: ModuleID = "com.iheart" %% "ficus" % v.ficus

  lazy val silhouettePlay: ModuleID = "com.mohiva" %% "play-silhouette" % v.silhouette
  lazy val silhouettePlayCas: ModuleID = "com.mohiva" %% "play-silhouette-cas" % v.silhouette
  lazy val silhouettePlayPasswordBcrypt: ModuleID = "com.mohiva" %% "play-silhouette-password-bcrypt" % v.silhouette
  lazy val silhouettePlayPersistence: ModuleID = "com.mohiva" %% "play-silhouette-persistence" % v.silhouette
  lazy val silhouettePlayCryptoJca: ModuleID = "com.mohiva" %% "play-silhouette-crypto-jca" % v.silhouette
  // Tests
  lazy val silhouettePlayTestkit: ModuleID = "com.mohiva" %% "play-silhouette-testkit" % v.silhouette % Test

  lazy val avro4sCore: ModuleID = "com.sksamuel.avro4s" %% "avro4s-core" % v.avro4s

  lazy val sangria: ModuleID = "org.sangria-graphql" %% "sangria" % v.sangria
  lazy val sangriaRelay: ModuleID = "org.sangria-graphql" %% "sangria-relay" % v.sangria
  lazy val snagriaPlayJson: ModuleID = "org.sangria-graphql" %% "sangria-play-json" % v.sangriaPlayJson
  lazy val sangriaSlowlog: ModuleID = "org.sangria-graphql" %% "sangria-slowlog" % v.sangriaSlowlog

  lazy val grpcNetty: ModuleID = "io.grpc" % "grpc-netty" % v.grpc
  lazy val grpcProtobuf: ModuleID = "io.grpc" % "grpc-protobuf" % v.grpc
  lazy val grpcStub: ModuleID = "io.grpc" % "grpc-stub" % v.grpc
  lazy val scalapbRuntime: ModuleID = "com.trueaccord.scalapb" %% "scalapb-runtime" % v.scalapbGrpc % "protobuf"
  lazy val scalapbRuntimeGrpc: ModuleID = "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % v.scalapbGrpc

  lazy val flyway: ModuleID = "org.flywaydb" %% "flyway-play" % v.flyway

  lazy val akkaRemote: ModuleID = "com.typesafe.akka" %% "akka-remote" % v.akka

  lazy val kafka: ModuleID = "org.apache.kafka" %% "kafka" % v.kafka excludeAll(ExclusionRule("org.slf4j"), ExclusionRule("log4j"))
  lazy val kafkaAvroSerializer: ModuleID = "io.confluent" % "kafka-avro-serializer" % v.confluent

  // Tests
  lazy val scalatestplus: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % v.scalatestplus % Test
  lazy val mokito: ModuleID = "org.mockito" % "mockito-core" % v.mokito % Test


  lazy val slickAll = Seq(postgresql, slickPlay, slickPlayEvolutions, slickPg, slickPgPlayJson, gcpPostgresSocketFactory)
  lazy val twirlAll = Seq(twirlCompiler, twirlApi, twirlParser)
  lazy val elasticAll = Seq(elastic4sCore, elastic4sHttp, elastic4sStreams, elastic4sPlayJson)
  lazy val silhouetteAll = Seq(silhouettePlay, silhouettePlayCas, silhouettePlayPasswordBcrypt, silhouettePlayPersistence, silhouettePlayCryptoJca)
  lazy val sangriaAll = Seq(sangria, sangriaRelay, snagriaPlayJson, sangriaSlowlog)
  lazy val bootstrapAll = Seq(bootstrap, bootstrapPlay, webjars)
  lazy val grcpAll = Seq(grpcNetty, grpcProtobuf, grpcStub, scalapbRuntime, scalapbRuntimeGrpc)
  lazy val kafkaAll = Seq(kafka, kafkaAvroSerializer)

  lazy val testAll = Seq(scalatestplus, mokito)
}
