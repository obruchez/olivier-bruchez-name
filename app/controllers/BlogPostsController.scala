package controllers

import actors.Cache
import models._
import models.blog.{Post, Posts}
import play.api.Logging
import play.api.mvc._
import util.Date._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class BlogPostsController @Inject() (implicit
    ec: ExecutionContext,
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging {
  import BlogPostsController._

  def blogPost(path: String) = Action.async { (request: Request[AnyContent]) =>
    // Blogger's addresses end with ".html": same path here, without it
    if (path.endsWith(".html"))
      Future.successful(
        MovedPermanently(routes.BlogPostsController.blogPost(path.stripSuffix(".html")).url)
      )
    else
      Cache.get(Posts).flatMap { posts =>
        posts.postFromPath(path) match {
          case Some(post) =>
            content(post).map {
              case Some(html) =>
                val index = posts.listItems.indexOf(post)
                // The list is most recent first
                val newer = posts.listItems.lift(index - 1)
                val older = posts.listItems.lift(index + 1)
                // Disqus knows a post that came from Blogger by the address it had there
                val canonicalUrl = s"https://${request.host}${post.url}"
                val commentsUrl = post.originalUrl.getOrElse(canonicalUrl)
                Ok(
                  views.html
                    .blogPost(post.page, post, html, newer, older, canonicalUrl, commentsUrl)
                )
              case None =>
                InternalServerError(views.html.error(post.page))
            }
          case None =>
            Future.successful(NotFound(views.html.notFound(request.path)))
        }
      }
  }

  // Atom feed of the latest posts, with their full text
  def feed = Action.async { (request: Request[AnyContent]) =>
    def absolute(path: String): String = s"https://${request.host}$path"
    def atomDate(post: Post): String = post.date.isoString.take(10) + "T12:00:00Z"

    for {
      posts <- Cache.get(Posts)
      latest = posts.listItems.take(FeedSize)
      contents <- Future.sequence(latest.map(content))
    } yield {
      val xml =
        <feed xmlns="http://www.w3.org/2005/Atom">
          <title>Olivier Bruchez: blog</title>
          <id>{absolute(routes.Application.blogPosts.url)}</id>
          <link href={absolute(routes.Application.blogPosts.url)}/>
          <link rel="self" href={absolute(routes.BlogPostsController.feed.url)}/>
          <author><name>Olivier Bruchez</name></author>
          <updated>{latest.headOption.fold("1970-01-01T00:00:00Z")(atomDate)}</updated>
          {
          latest.zip(contents).map { case (post, html) =>
            <entry>
              <title>{post.title}</title>
              <id>{post.feedId.getOrElse(absolute(post.url))}</id>
              <link href={absolute(post.url)}/>
              <updated>{atomDate(post)}</updated>
              {
              html.toSeq.map(html =>
                <content type="html">{absoluteLinks(html.htmlString, absolute)}</content>
              )
            }
            </entry>
          }
        }
        </feed>

      Ok("""<?xml version="1.0" encoding="UTF-8"?>""" + "\n" + xml.toString)
        .as("application/atom+xml")
    }
  }

  private def content(post: Post): Future[Option[HtmlContent]] =
    Cache
      .fileContent(post.fileSource)
      .map {
        case markdown: MarkdownContent =>
          markdown.toHtmlBlocks match {
            case Success(html) =>
              Some(html)
            case Failure(throwable) =>
              logger.error(s"Could not render the post ${post.path}", throwable)
              None
          }
        case _ =>
          None
      }
      .recover { case throwable =>
        logger.error(s"Could not read the post ${post.path}", throwable)
        None
      }
}

object BlogPostsController {
  private val FeedSize = 10

  // Feed readers show the posts away from the site: links and images within the site need its address
  private def absoluteLinks(html: String, absolute: String => String): String =
    html.replaceAll("""(href|src)="(/[^"/][^"]*)"""", "$1=\"" + absolute("") + "$2\"")
}
