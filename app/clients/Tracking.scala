package clients

import javax.inject.{Inject, Singleton}

import play.api.Configuration

/**
  * @author im.
  */
@Singleton
class Tracking @Inject() (config: Configuration) {

  //separate track email, telegram etc, flag or other stuff
  def trackClick(url: String): String = {
    "track/click"
  }
}
