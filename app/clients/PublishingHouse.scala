package clients

import java.lang.invoke.MethodHandles
import java.net.URL
import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import clients.Compiler.HtmlTemplate
import models.{Edition, Newsletter, Post, PostView}
import org.slf4j.LoggerFactory
import play.api.Configuration

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

  sealed trait PublishingMaterials

  // options will contain post type: urls, interesting text, thoughts or else

  //TODO: move to models
  case class ReadyPost(
                        id: Option[Long],
                        title: String,
                        titleUrl: String,
                        annotation: String,
                        body: HtmlTemplate,
                        references: List[URL],
                        config: Configuration,
                        target: Target.Value)

  case class ReadyEdition(id: Option[Long],
                          newsletter: Newsletter,
                          date: LocalDate,
                          url: Option[URL],
                          title: Option[String],
                          header: Option[HtmlTemplate], //TODO: make it optional in the model
                          footer: Option[HtmlTemplate], //TODO: make it optional in the model
                          posts: List[ReadyPost],
                          config: Configuration,
                          target: Target.Value)

  case class ReadyPostView(post: ReadyPost, edition: Option[Edition], prev: Option[ReadyPost], next: Option[ReadyPost])

}

class CompilerCache(compiler: Compiler)(implicit ec: ExecutionContext) {

  import PublishingHouse._
  import com.github.benmanes.caffeine.cache.Caffeine

  import scalacache._
  import caffeine._
  import memoization._
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

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  private val cache = new CompilerCache(compiler)

  private def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = {
    cache.compile(tags, target)
  }

  def doPost(post: Post, target: Target.Value): Future[ReadyPost] = {
    logger.info(s"compiling post, id=${post.id}, userId=${post.userId}, title=${post.title}")
    compile(quill.toTagStr(post.body), target)
      .map(html => ReadyPost(post.id, post.title, post.titleUrl, post.annotation, html, post.refs, Configuration.from(post.options), target))
  }

  def doPosts(posts: List[Post], target: Target.Value): Future[List[ReadyPost]] = {
    Future.sequence(posts.map(post => doPost(post, target)))
  }

  def doOptionPosts(posts: List[Option[Post]], target: Target.Value): Future[List[Option[ReadyPost]]] = {
    Future.sequence(posts.map(post => post.fold(Future(Option.empty[ReadyPost]))(p => doPost(p, target).map(Some(_)))))
  }

  def doPostView(postView: PostView, target: Target.Value): Future[ReadyPostView] = {
    for {
      post <- doPost(postView.post, target)
      prev <- postView.prev.fold(Future(Option.empty[ReadyPost]))(p => doPost(p, target).map(Some(_)))
      next <- postView.next.fold(Future(Option.empty[ReadyPost]))(p => doPost(p, target).map(Some(_)))
    } yield ReadyPostView(post, postView.edition, prev, next)
  }

  def doEdition(edition: Edition, target: Target.Value): Future[ReadyEdition] = {
    logger.info(s"compiling edition, id=${edition.id}, newsletterId=${edition.newsletter.name}, title=${edition.title}")
    for {
      header <- if (edition.header.isDefined) compile(quill.toTagStr(edition.header.get), target).map(Some(_)) else Future(None)
      footer <- if (edition.footer.isDefined) compile(quill.toTagStr(edition.footer.get), target).map(Some(_)) else Future(None)
      posts <- doPosts(edition.posts, target)
    } yield ReadyEdition(edition.id, edition.newsletter, edition.date, edition.url, edition.title, header, footer, posts, Configuration.from(edition.options), target)
  }
}
