package clients

import javax.inject.{Inject, Singleton}

import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * @author im.
  */
@Singleton
class Telegram @Inject() (appLifecycle: ApplicationLifecycle, config: Configuration) extends TelegramBot {

  lazy private val telegramConfig = config.get[Configuration]("telegram")
  lazy private val apiToken = telegramConfig.get[String]("token")
  lazy private val channel = telegramConfig.get[String]("channel")
  Logger.info(s"chat: $channel")
  require(channel.startsWith("@"), "channel name must starts with '@'")

  override def token: String = apiToken

  run()
  Logger.info(s"$getClass: started")
  appLifecycle.addStopHook { () =>
    Logger.info(s"$getClass: shutdown")
    shutdown() }

  def sendMessage(message: String): Future[Message] = {
    request(SendMessage(channel, message))
  }

  def sendPicture(url: String): Future[Message] = {
    request(SendPhoto(channel, url))
  }
}