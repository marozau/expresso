package config

import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterEach, WordSpecLike}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

/**
  * @author im.
  */
//https://www.playframework.com/documentation/2.6.x/ScalaTestingWithGuice

trait TestContext extends PlaySpec
  with GuiceOneAppPerSuite
  with BeforeAndAfterEach
  with MockitoSugar
  with ScalaFutures
  with WordSpecLike
  with Eventually {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(1000, Millis)), scaled(Span(100, Millis)))

  override def fakeApplication() = {
    new GuiceApplicationBuilder()
      .build()
  }

  private val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  val database = new TestDatabase(dbConfigProvider)


  override protected def beforeEach(): Unit = {
    database.cleanAll()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    database.cleanAll()
  }
}
