package controllers

import actors.Cache
import models.Page
import models.blogger.{Post, Posts}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

object BlogPostsController extends Controller {
  def blogPost(relativePermalink: String) = Action.async {
    Cache.get(Posts) map { posts =>
      posts.listItems.find(_.relativePermalink == relativePermalink) match {
        case Some(post) =>
          Ok(views.html.blogPost(post.page, post))
        case None =>
          NotFound
      }
    }
  }

  implicit class PostOps(post: Post) {
    def page: Page =
      Page(title = post.title,
           url = routes.BlogPostsController.blogPost(post.relativePermalink).url,
           icon = Sitemap.blog.icon)
  }
}
