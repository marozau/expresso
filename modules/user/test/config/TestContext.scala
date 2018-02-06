package config

import api.GrpcServer
import org.scalactic.source.Position
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
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
  with MockitoSugar {

  override def fakeApplication() = {
    val mockGrpcServer = mock[GrpcServer]
    new GuiceApplicationBuilder()
      .overrides(bind[GrpcServer].toInstance(mockGrpcServer))
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
