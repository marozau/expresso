package config

import java.time.Instant

import clients.Quartz
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
import today.expresso.stream.Producer
import today.expresso.stream.domain.model.user.User

/**
  * @author im.
  */
//https://www.playframework.com/documentation/2.6.x/ScalaTestingWithGuice

object TestContext {

  val userId = 1L
  val userEmail = "test@expresso.today"
  val user = User(userId, userEmail, User.Status.VERIFIED, List(User.Role.EDITOR), None, None, None, Instant.now())
}

trait TestContext extends PlaySpec with StreamSpec
  with GuiceOneAppPerSuite
  with BeforeAndAfterEach
  with MockitoSugar
  with ScalaFutures
  with WordSpecLike
  with Eventually {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(100, Millis)))

  val mockUserService = mock[UserService] //TODO: mock UserServiceGrpc instead
  val mockMailService = mock[MailService] //TODO: mock MailClient instead

  override def fakeApplication() = {

    new GuiceApplicationBuilder()
      .disable(classOf[play.api.cache.redis.RedisCacheModule])
      .overrides(bind[UserService].toInstance(mockUserService))
      .overrides(bind[MailService].toInstance(mockMailService))
      .overrides(bind[Producer].toInstance(mockProducer))
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
