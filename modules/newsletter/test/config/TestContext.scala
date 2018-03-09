package config

import api.GrpcServer
import clients.{Quartz, QuartzFutureImpl}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterEach, WordSpecLike}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{MailService, UserService}
import today.expresso.grpc.user.domain.User

/**
  * @author im.
  */
//https://www.playframework.com/documentation/2.6.x/ScalaTestingWithGuice

object TestContext {

  val userId = 1L
  val userEmail = "test@expresso.today"
  val user = User(userId, userEmail, User.Status.VERIFIED, Seq(User.Role.EDITOR))
}

trait TestContext extends PlaySpec
  with GuiceOneAppPerSuite
  with BeforeAndAfterEach
  with MockitoSugar
  with ScalaFutures
  with WordSpecLike
  with Eventually {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(1000, Millis)), scaled(Span(100, Millis)))

  val mockGrpcServer = mock[GrpcServer]
  val mockUserService = mock[UserService]
  val mockMailService = mock[MailService]

  override def fakeApplication() = {

    new GuiceApplicationBuilder()
      .disable(classOf[play.api.cache.redis.RedisCacheModule])
      .overrides(bind[GrpcServer].toInstance(mockGrpcServer))
      .overrides(bind[UserService].toInstance(mockUserService))
      .overrides(bind[MailService].toInstance(mockMailService))
      .build()
  }

  private val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  val database = new TestDatabase(dbConfigProvider)

  private val quartz = app.injector.instanceOf[Quartz]

  override protected def beforeEach(): Unit = {
    quartz.clear()
    database.cleanAll()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    quartz.clear()
    database.cleanAll()
  }
}
