package filters

import javax.inject.Inject

import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import play.api.http.DefaultHttpFilters
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter

/**
  * Add the following filters by default to all projects
  *
  * https://www.playframework.com/documentation/latest/ScalaCsrf
  * https://www.playframework.com/documentation/latest/AllowedHostsFilter
  * https://www.playframework.com/documentation/latest/SecurityHeaders
  */
class Filters @Inject()() extends DefaultHttpFilters()