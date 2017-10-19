package clients

import java.net.URL
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import models.{NewsletterAndPosts, Post}
import play.api.{Configuration, Logger}
import clients.Compiler.HtmlTemplate

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
object PublishingHouse {

  object Target extends Enumeration {
    type Target = Value
    val DEV, EMAIL, TELEGRAM, VIBER, SITE, LANDING, ANALYTICS = Value
  }

  // TODO: move to analytics repository
  //  object Category extends Enumeration {
  //    type Type = Value
  //    val INTRO, PHILOSOPHY, TEXT, URLS, ADVERTISEMENT = Value
  //  }

  sealed trait PublishingPost

  // options will contain post type: urls, interesting text, thoughts or else

  //TODO: move to models
  case class ReadyPost(
                        id: Option[Long],
                        title: String,
                        annotation: String,
                        body: HtmlTemplate,
                        references: List[URL],
                        config: Configuration,
                        target: Target.Value) extends PublishingPost

  case class ReadyNewsletter(
                              id: Option[Long],
                              url: Option[String],
                              title: Option[String],
                              header: Option[HtmlTemplate], //TODO: make it optional in the model
                              footer: Option[HtmlTemplate], //TODO: make it optional in the model
                              posts: List[ReadyPost],
                              config: Configuration,
                              target: Target.Value,
                              publishTimestamp: Option[ZonedDateTime]) extends PublishingPost

}

class CompilerCache(compiler: Compiler)(implicit ec: ExecutionContext) {

  import PublishingHouse._
  import com.github.benmanes.caffeine.cache.Caffeine

  import scalacache._
  import memoization._
  import caffeine._
  import scala.concurrent.duration._

  private val underlyingCaffeineCache = Caffeine.newBuilder().maximumSize(100000L).build[String, Object]
  private implicit val scalaCache: ScalaCache[NoSerialization] = ScalaCache(CaffeineCache(underlyingCaffeineCache))

  def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = memoize(ttl(target)) {
    val body = Compiler.header + tags
    compiler.compile(body, Some("views.html." + target.toString.toLowerCase))
  }

  private def ttl(target: Target.Value) = target match {
    case Target.DEV => 10.minutes
    case _ => 2.days
  }
}

@Singleton
class PublishingHouse @Inject()(
                                 configuration: Configuration,
                                 quill: Quill,
                                 compiler: Compiler)(implicit ec: ExecutionContext) {

  import PublishingHouse._
  import implicits.PostImplicits._

  private val cache = new CompilerCache(compiler)

  private def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = {
    cache.compile(tags, target)
  }

  def doPost(post: Post, target: Target.Value): Future[ReadyPost] = {
    Logger.info(s"compiling post, id=${post.id}, userId=${post.userId}, title=${post.title}")
    compile(quill.toTagStr(post.body), target)
      .map(html => ReadyPost(post.id, post.title, post.annotation, html, post.refs, Configuration.from(post.options), target))
  }

  def doPosts(posts: List[Post], target: Target.Value): Future[List[ReadyPost]] = {
    Future.sequence(posts.map(post => doPost(post, target)))
  }

  def doOptionPosts(posts: List[Option[Post]], target: Target.Value): Future[List[Option[ReadyPost]]] = {
    Future.sequence(posts.map(post => post.fold(Future(Option.empty[ReadyPost]))(p => doPost(p, target).map(Some(_)))))
  }

  def doNewsletter(nl: NewsletterAndPosts, target: Target.Value): Future[ReadyNewsletter] = {
    Logger.info(s"compiling newsletter, id=${nl.id}, userId=${nl.userId}, title=${nl.title}")
    for {
      header <- if (nl.header.isDefined) compile(quill.toTagStr(nl.header.get), target).map(Some(_)) else Future(None)
      footer <- if (nl.footer.isDefined) compile(quill.toTagStr(nl.footer.get), target).map(Some(_)) else Future(None)
      posts <- doPosts(nl.posts, target)
    } yield ReadyNewsletter(nl.id, nl.url, nl.title, header, footer, posts, Configuration.from(nl.options), target, nl.publishTimestamp)
  }
}
