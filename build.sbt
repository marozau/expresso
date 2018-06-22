import Common._
import Dependency._

lazy val common = (project in file("modules/common"))
  .settings(Common.settings: _*)
  .settings(
    libraryDependencies ++= Seq(guice),
    libraryDependencies ++= silhouetteAll,
    libraryDependencies ++= slickAll,
    libraryDependencies += ficus,
    libraryDependencies += grpcProtobuf
  )

lazy val grpc = (project in file("modules/grpc"))
  .dependsOn(common)
  .settings(Common.settings: _*)
  .settings(
    libraryDependencies ++= grcpAll
  )

lazy val api = (project in file("modules/api"))
  .enablePlugins(PlayScala)
  .settings(Common.settings: _*)
  .settings(PlayKeys.devSettings := Seq("play.server.http.port" -> "9000"))
  .dependsOn(common, grpc)
  .settings(
    libraryDependencies ++= Seq(guice, filters, ws, cacheApi),
    libraryDependencies ++= silhouetteAll,
    libraryDependencies ++= slickAll,
    libraryDependencies ++= sangriaAll,
    libraryDependencies += quartz,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += redisPlay,
    libraryDependencies ++= bootstrapAll,
    libraryDependencies ++= Seq(playMailer, playMailerGuice),
    libraryDependencies += flyway,
    libraryDependencies += akkaRemote
  )

lazy val user = (project in file("modules/user"))
  .enablePlugins(PlayScala)
  .settings(Common.settings: _*)
  .settings(PlayKeys.devSettings := Seq("play.server.http.port" -> "9001"))
  .dependsOn(common, grpc, stream)
  .settings(
    libraryDependencies ++= Seq(guice, filters, ws, cacheApi),
    libraryDependencies ++= silhouetteAll,
    libraryDependencies ++= slickAll,
    libraryDependencies += quartz,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += redisPlay,
    libraryDependencies ++= bootstrapAll,
    libraryDependencies ++= Seq(playMailer, playMailerGuice),
    libraryDependencies += flyway,

    libraryDependencies ++= testAll,
    libraryDependencies += ehcache % Test
  )

lazy val newsletter = (project in file("modules/newsletter"))
  .enablePlugins(PlayScala)
  .settings(Common.settings: _*)
  .settings(PlayKeys.devSettings := Seq("play.server.http.port" -> "9002"))
  .dependsOn(common, templates, stream)
  .settings(
    libraryDependencies ++= Seq(guice, filters, ws, cacheApi),
    libraryDependencies ++= silhouetteAll,
    libraryDependencies ++= slickAll,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += redisPlay,
    libraryDependencies += scalacache,
    libraryDependencies ++= Seq(playMailer, playMailerGuice),
    libraryDependencies ++= twirlAll,
    libraryDependencies += reflections,
    libraryDependencies ++= bootstrapAll,
    libraryDependencies += playHtmlCompressor,
    libraryDependencies += avro4sCore,
    libraryDependencies += quartz,
    libraryDependencies += telegrambot4s,
    libraryDependencies += viber,
    libraryDependencies += flyway,

    libraryDependencies ++= testAll,
    libraryDependencies += ehcache % Test
  )

lazy val templates = (project in file("modules/templates"))
  .enablePlugins(SbtTwirl)
  .dependsOn(common)
  .settings(Common.settings: _*)
  .settings(
    libraryDependencies += play % Provided,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += scalacache,
    libraryDependencies ++= twirlAll,
    libraryDependencies += reflections,
    libraryDependencies += playHtmlCompressor,

    libraryDependencies ++= testAll
  )
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "play.api.templates.PlayMagic._",
      "play.api.i18n._",
      "play.api.mvc._",
      "play.api.data._",
      "today.expresso.templates.api.domain._",
      "today.expresso.templates.impl.domain._"
    ),
    TwirlKeys.constructorAnnotations += "@javax.inject.Inject()"
  )

// TODO: move all domain protobuf and avro models to the separate module
lazy val stream = (project in file("modules/stream"))
  .enablePlugins(SbtTwirl)
  .dependsOn(common)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(Common.settings: _*)
  .settings(
    libraryDependencies += play % Provided,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += avro4sCore,
    libraryDependencies ++= kafkaAll,

    libraryDependencies ++= testAllWithIT,
    libraryDependencies += reflections % "it",
    libraryDependencies += guava % "it"
  )

lazy val payment = (project in file("modules/payment"))
  .enablePlugins(PlayScala)
  .settings(Common.settings: _*)
  .settings(PlayKeys.devSettings := Seq("play.server.http.port" -> "9003"))
  .dependsOn(common, stream)
  .settings(
    libraryDependencies ++= Seq(guice, filters, ws, cacheApi),
    libraryDependencies ++= slickAll,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += quartz,
    libraryDependencies += flyway,

    libraryDependencies ++= testAll
  )


lazy val expresso = (project in file("."))
  .settings(Common.settings: _*)
  .settings(Manifest.manifestSettings: _*)
