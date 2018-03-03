package today.expresso.templates.impl

import javax.inject.{Inject, Singleton}

import today.expresso.templates.api.HtmlCompiler
import today.expresso.templates.api.HtmlCompiler.HtmlTemplate
import today.expresso.templates.api.domain.Target

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
trait CompilerCache {
  def compile(tags: String, target: Target.Value): Future[HtmlTemplate]
}

@Singleton
class CompilerCacheImpl @Inject() (compiler: HtmlCompiler)(implicit ec: ExecutionContext) extends CompilerCache {

  import com.github.benmanes.caffeine.cache.Caffeine

  import scalacache._
  import caffeine._
  import memoization._
  import scala.concurrent.duration._

  private val underlyingCaffeineCache = Caffeine.newBuilder().maximumSize(100000L).build[String, Object]
  private implicit val scalaCache: ScalaCache[NoSerialization] = ScalaCache(CaffeineCache(underlyingCaffeineCache))

  override def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = memoize(ttl(target)) {
    compiler.compileAsync(tags, Some("templates.compiler." + target.toString.toLowerCase + ".html"))
  }

  private def ttl(target: Target.Value) = target match {
    case Target.DEV => 1.hour
    case _ => 2.days
  }
}

