package implicits

/**
  * @author im.
  */
object NewsletterImplicits {

  import PostImplicits._
  import controllers.NewsletterController._
  import models._

  implicit def optionStringCast(str: Option[String]): String = str.getOrElse("")

  implicit def newsletterCast(nl: NewsletterAndPosts): Newsletter = {
    Newsletter(nl.id, nl.userId, nl.url, nl.title, nl.header, nl.footer, nl.posts, nl.options)
  }
}
