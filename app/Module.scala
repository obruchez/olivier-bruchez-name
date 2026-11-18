import actors.{Cache, Master}
import com.google.inject.AbstractModule
import controllers.Sitemap
import models.Fetchable
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment, Logging, Mode}
import util.Thumbnails

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/** Loaded automatically by Play (root package `Module`). */
class Module extends AbstractModule {
  override def configure(): Unit =
    bind(classOf[Startup]).asEagerSingleton()
}

/** Ties the fetching/caching actors to the application lifecycle. */
@Singleton
class Startup @Inject() (
    configuration: Configuration,
    environment: Environment,
    lifecycle: ApplicationLifecycle
) extends Logging {
  util.Configuration.configuration = configuration

  Master.start()

  lifecycle.addStopHook(() => Master.stop())

  // Not while developing, unless asked: every restart would read and resize all the images again
  if (
    configuration.getOptional[Boolean]("thumbnails.warmup").getOrElse(environment.mode == Mode.Prod)
  )
    Sitemap.fetchables.foreach(warmUpThumbnails(_, attemptsLeft = 5))

  /** Gets the covers of a list ready as soon as the list is known, rather than at its first visit.
    */
  private def warmUpThumbnails(fetchable: Fetchable, attemptsLeft: Int): Unit = {
    given ExecutionContext = Master.system.dispatcher

    Cache
      .get(fetchable)
      .map { cacheable =>
        val keys =
          cacheable.listItems.flatMap(_.image).map(Thumbnails.key(Thumbnails.Covers, _))

        if (keys.nonEmpty) {
          logger.info(s"Warming up ${keys.size} thumbnails (${fetchable.name})")
          Thumbnails.warmUp(keys, Thumbnails.Covers)
        }
      }
      .failed
      .foreach { _ =>
        // The list is not fetched yet
        if (attemptsLeft > 1)
          Master.system.scheduler.scheduleOnce(30.seconds) {
            warmUpThumbnails(fetchable, attemptsLeft - 1)
          }
      }
  }
}
