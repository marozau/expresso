package services

import java.net.URL
import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}

import models.{NewsletterAndPosts, Post}
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.{Configuration, Logger}
import services.Compiler.HtmlTemplate

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

@Singleton
class PublishingHouse @Inject()(
                                 configuration: Configuration,
                                 quill: Quill,
                                 compiler: Compiler,
                                 @NamedCache("publish-cache") cache: AsyncCacheApi)(implicit ec: ExecutionContext) {

  import PublishingHouse._
  import implicits.PostImplicits._

  import scala.concurrent.duration._

  private def cacheKey(body: String, target: Target.Value) = s"$target-${body.hashCode}"

  private def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = {
    val body = Compiler.header + tags
    cache.getOrElseUpdate[HtmlTemplate](cacheKey(body, target), 2.days) { //TODO: calc duration based on publication type and post create timestamp
      compiler.compile(body, Some("views.html." + target.toString.toLowerCase))
    }
  }

  def doPost(post: Post, target: Target.Value): Future[ReadyPost] = {
    Logger.info(s"compiling post, id=${post.id}, userId=${post.userId}, title=${post.title}")
    compile(quill.toTagStr(post.body), target)
      .map(html => ReadyPost(post.id, post.title, post.annotation, html, post.refs, Configuration.from(post.options), target))
  }

  def doPosts(posts: List[Post], target: Target.Value): Future[List[ReadyPost]] = {
    Future.sequence(posts.map(post => doPost(post, target)))
  }

  def doNewsletter(nl: NewsletterAndPosts, target: Target.Value): Future[ReadyNewsletter] = {
    Logger.info(s"compiling newsletter, id=${nl.id}, userId=${nl.userId}, title=${nl.title}")
    for {
      header <- if (nl.header.isDefined) compile(quill.toTagStr(nl.header.get), target).map(Some(_)) else Future(None)
      footer <- if (nl.footer.isDefined) compile(quill.toTagStr(nl.footer.get), target).map(Some(_)) else Future(None)
      posts <-  doPosts(nl.posts, target)
    } yield ReadyNewsletter(nl.id, nl.url, nl.title, header, footer, posts, Configuration.from(nl.options), target, nl.publishTimestamp)
  }
}
