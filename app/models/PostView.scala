package models

import exceptions.PostNotFoundException

/**
  * @author im.
  */
case class PostView(post: Post, edition: Option[Edition] = None, prev: Option[Post] = None, next: Option[Post] = None)

object PostView {
  def apply(edition: Edition, titleUrl: String): PostView = {
    val postsx = None +: edition.posts.map(Some(_)) :+ None
    val slice = postsx.sliding(3).filter(slice => slice(1).get.titleUrl.equals(titleUrl)).toList.headOption
    if (slice.isEmpty) throw PostNotFoundException(0, s"postView failed, title=$titleUrl not found")
    PostView(slice.get(1).get, Some(edition), slice.get.head, slice.get.last)
  }
}
