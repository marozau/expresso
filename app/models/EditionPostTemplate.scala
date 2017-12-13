package models

/**
  * @author im.
  */
case class EditionPostTemplate(post: PostTemplate,
                               edition: Option[Edition],
                               prev: Option[PostTemplate],
                               next: Option[PostTemplate])
