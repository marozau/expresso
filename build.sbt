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
    libraryDependencies += quartz,
    libraryDependencies += ficus,
    libraryDependencies += scalaGuice,
    libraryDependencies += redisPlay,
    libraryDependencies ++= bootstrapAll,
    libraryDependencies ++= Seq(playMailer, playMailerGuice),
    libraryDependencies += flyway
  )

lazy val user = (project in file("modules/user"))
  .enablePlugins(PlayScala)
  .settings(Common.settings: _*)
  .settings(PlayKeys.devSettings := Seq("play.server.http.port" -> "9001"))
  .dependsOn(common, grpc)
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
    libraryDependencies += flyway
  )

lazy val newsletter = (project in file("modules/newsletter"))
  .enablePlugins(PlayScala)
  .settings(Common.settings: _*)
  .settings(PlayKeys.devSettings := Seq("play.server.http.port" -> "9002"))
  .dependsOn(common, grpc)
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
    libraryDependencies += viber
  )

lazy val expresso = (project in file("."))
  .settings(Common.settings: _*)
  .settings(Manifest.manifestSettings: _*)
