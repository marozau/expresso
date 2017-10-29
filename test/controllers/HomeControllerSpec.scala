package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with OneAppPerTest {

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      "Ok" mustBe "Ok"
    }
  }
}
