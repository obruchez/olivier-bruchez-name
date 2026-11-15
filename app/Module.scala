import actors.Master
import com.google.inject.AbstractModule
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}

/** Loaded automatically by Play (root package `Module`). */
class Module extends AbstractModule {
  override def configure(): Unit =
    bind(classOf[Startup]).asEagerSingleton()
}

/** Ties the fetching/caching actors to the application lifecycle. */
@Singleton
class Startup @Inject() (configuration: Configuration, lifecycle: ApplicationLifecycle) {
  util.Configuration.configuration = configuration

  Master.start()

  lifecycle.addStopHook(() => Master.stop())
}
