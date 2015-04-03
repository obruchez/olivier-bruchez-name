import actors.Master
import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Master.start()
  }

  override def onStop(app: Application): Unit = {
    Master.stop()
  }
}
