package today.expresso.templates.impl

import java.lang.invoke.MethodHandles
import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.Configuration
import today.expresso.templates.api.HtmlCompiler
import today.expresso.templates.api.HtmlCompiler.HtmlTemplate
import today.expresso.templates.api.domain.{Edition, Post, Target}
import today.expresso.templates.impl.domain.{EditionTemplate, PostTemplate}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
class CompilerCache(compiler: HtmlCompiler)(implicit ec: ExecutionContext) {

  import com.github.benmanes.caffeine.cache.Caffeine

  import scalacache._
  import caffeine._
  import memoization._
  import scala.concurrent.duration._

  private val underlyingCaffeineCache = Caffeine.newBuilder().maximumSize(100000L).build[String, Object]
  private implicit val scalaCache: ScalaCache[NoSerialization] = ScalaCache(CaffeineCache(underlyingCaffeineCache))

  def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = memoize(ttl(target)) {
    compiler.compileAsync(tags, Some("templates.compiler." + target.toString.toLowerCase + ".html"))
  }

  private def ttl(target: Target.Value) = target match {
    case Target.DEV => 1.hour
    case _ => 2.days
  }
}

@Singleton
class CompilerService @Inject()(quill: QuillParser, compiler: HtmlCompiler)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  private val cache = new CompilerCache(compiler)

  private def compile(tags: String, target: Target.Value): Future[HtmlTemplate] = {
    cache.compile(tags, target)
  }

  def doPost(post: Post, target: Target.Value): Future[PostTemplate] = {
    logger.info(s"compiling post, id=${post.id}, userId=${post.userId}, title=${post.title}")
    compile(quill.jsonToTagStr(post.body), target)
      .map(html => PostTemplate(post.id, post.url, post.title, post.annotation, html, Configuration.empty, target)) //TODO: Configuration.from(post.options)
  }

  def doPosts(posts: List[Post], target: Target.Value): Future[List[PostTemplate]] = {
    Future.sequence(posts.map(post => doPost(post, target)))
  }

  def doOptionPosts(posts: List[Option[Post]], target: Target.Value): Future[List[Option[PostTemplate]]] = {
    Future.sequence(posts.map(post => post.fold(Future(Option.empty[PostTemplate]))(p => doPost(p, target).map(Some(_)))))
  }

  //  def doPostView(postView: PostView, target: Target.Value): Future[EditionPostTemplate] = {
  //    for {
  //      post <- doPost(postView.post, target)
  //      prev <- postView.prev.fold(Future(Option.empty[PostTemplate]))(p => doPost(p, target).map(Some(_)))
  //      next <- postView.next.fold(Future(Option.empty[PostTemplate]))(p => doPost(p, target).map(Some(_)))
  //    } yield EditionPostTemplate(post, postView.edition, prev, next)
  //  }

  def doEdition(edition: Edition, target: Target.Value): Future[EditionTemplate] = {
    logger.info(s"compiling edition, id=${edition.id}, newsletterId=${edition.newsletter.name}, title=${edition.title}")
    for {
      header <- if (edition.header.isDefined) compile(quill.jsonToTagStr(edition.header.get), target).map(Some(_)) else Future(None)
      footer <- if (edition.footer.isDefined) compile(quill.jsonToTagStr(edition.footer.get), target).map(Some(_)) else Future(None)
      posts <- doPosts(edition.posts, target)
    } yield EditionTemplate(edition.id, edition.url, edition.newsletter, edition.date, edition.title, posts, header, footer, Configuration.empty, target) //TODO: Configuration.from(edition.options)
  }
}
