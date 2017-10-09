package services

import javax.inject.{Inject, Singleton}

import com.google.common.util.concurrent.MoreExecutors
import com.viber.bot.api.ViberBot
import com.viber.bot.message.Message
import com.viber.bot.profile.{BotProfile, UserProfile}
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * @author im.
  */
@Singleton
class Viber @Inject()(config: Configuration)(implicit ec: ExecutionContext) {

  import Viber._

  private val viberConfig = config.get[Configuration]("viber")
  private val receiver = createUserProfile(
    viberConfig.get[String]("to.id"),
    viberConfig.get[String]("to.country"),
    viberConfig.get[String]("to.language"),
    viberConfig.get[Int]("to.api_version"),
    viberConfig.getOptional[String]("to.name"),
    viberConfig.getOptional[String]("to.avatar"))

  private val bot = new ViberBot(new BotProfile(config.get[String]("name")), viberConfig.get[String]("token"))

  def sendMessage(message: Seq[Message]): Future[Seq[String]] = {
    bot.sendMessage(receiver, message.asJavaCollection)
      .map(_.asScala.toSeq)
  }
}

object Viber {

  import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

  implicit def toScalaFuture[T](lf: ListenableFuture[T]): Future[T] = {
    val p = Promise[T]()
    Futures.addCallback(lf, new FutureCallback[T] {

      override def onFailure(t: Throwable): Unit = p failure t

      override def onSuccess(result: T): Unit = p success result
    }, MoreExecutors.directExecutor())
    p.future
  }

  import scala.reflect._

  def createUserProfile(id: String,
                        country: String,
                        language: String,
                        apiVersion: Int,
                        name: Option[String],
                        avatar: Option[String])(implicit ct: ClassTag[UserProfile]): UserProfile = {
    val ctor = ct.runtimeClass.getDeclaredConstructor(
      classOf[String], classOf[String], classOf[String], classOf[Int], classOf[String], classOf[String])
    ctor.setAccessible(true)
    ctor.newInstance(
      id, country, language, Int.box(apiVersion), name.orNull, avatar.orNull).asInstanceOf[UserProfile]
  }
}
