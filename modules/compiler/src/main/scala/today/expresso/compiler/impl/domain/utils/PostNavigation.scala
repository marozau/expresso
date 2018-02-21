package today.expresso.compiler.impl.domain.utils

import play.api.Logger
import today.expresso.compiler.impl.domain.{EditionTemplate, PostTemplate}

/**
  * @author im.
  */
case class PostNavigation(post: PostTemplate, prev: Option[PostTemplate] = None, next: Option[PostTemplate] = None)

object PostNavigation {
  def apply(edition: EditionTemplate, postId: Long): PostNavigation = {
    Logger.info(s"$edition")
    val postsx = None +: edition.posts.map(Some(_)) :+ None
    val slice = postsx.sliding(3).filter(slice => slice(1).get.id.equals(postId)).toList.headOption
    if (slice.isEmpty) throw new RuntimeException(s"postView failed, postId=$postId not found")
    PostNavigation(slice.get(1).get, slice.get.head, slice.get.last)
  }
}
