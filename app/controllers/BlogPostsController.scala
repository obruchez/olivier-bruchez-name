package controllers

import actors.Cache
import models.Page
import models.blogger.{Post, Posts}
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BlogPostsController @Inject() (implicit
    ec: ExecutionContext,
    val controllerComponents: ControllerComponents
) extends BaseController {
  def blogPost(relativePermalink: String) = Action.async {
    Cache.get(Posts) map { posts =>
      posts.listItems.find(_.relativePermalink == relativePermalink) match {
        case Some(post) =>
          // 2022-11-30: do not redirect to original blog post until Google has re-indexed everything correctly
          NotFound
        // 2022-09-05: redirect instead of serving the blog post contents, for now
        // Redirect(post.originalUrl)
        // Ok(views.html.blogPost(post.page, post))
        case None =>
          NotFound
      }
    }
  }

  implicit class PostOps(post: Post) {
    def page: Page =
      Page(
        title = post.title,
        url = routes.BlogPostsController.blogPost(post.relativePermalink).url,
        icon = Sitemap.blog.icon
      )
  }
}
