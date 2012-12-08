package bootstrap.liftweb

import org.bruchez.olivier.login.OpenIdVendor
import org.bruchez.olivier.model._
import org.bruchez.olivier.snippet._
import net.liftmodules.JQueryModule
import net.liftweb.common._
import net.liftweb.mapper.{Countries => _, _}
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util._

class Boot extends Loggable {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("org.bruchez.olivier")

    JQueryModule.InitParam.JQuery=JQueryModule.JQuery172
    JQueryModule.init() 
 
    /*un-comment and switch to db of your liking */
    MySchemaHelper.initSquerylRecordWithInMemoryDB()
    //MySchemaHelper.initSquerylRecordWithMySqlDB()
    //MySchemaHelper.initSquerylRecordWithPostgresDB()

    Props.mode match {
      case Props.RunModes.Development => {
        logger.info("RunMode is DEVELOPMENT")
        /*OBS! do no use this in a production env*/
        if (Props.getBool("db.schemify", defVal = false)) MySchemaHelper.dropAndCreateSchema()
        // pass paths that start with 'console' to be processed by the H2Console servlet
        if (MySchemaHelper.isUsingH2Driver) {
          /* make db console browser-accessible in dev mode at /console 
           * see http://www.h2database.com/html/tutorial.html#tutorial_starting_h2_console 
           * Embedded Mode JDBC URL: jdbc:h2:mem:test User Name:test Password:test */
          logger.info("Set up H2 db console at /console ")
          LiftRules.liftRequest.append({
            case r if (r.path.partPath match { case "console" :: _ => true case _ => false }) => false
          })
        }

        // @todo Squeryl vs Mapper => use only one
        val vendor = new StandardDBVendor(
            Props.get("db.driver") openOr "org.h2.Driver",
            Props.get("db.url") openOr "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
            Props.get("db.user"), Props.get("db.password"))
        LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
        DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
        Schemifier.schemify(true, Schemifier.infoF _, User)
      }
      case Props.RunModes.Production => logger.info("RunMode is PRODUCTION")
      case _ => logger.info("RunMode is TEST, PILOT or STAGING")
    }

    //val LoginRequired = If(() => User.loggedIn_?, () => RedirectResponse("/"))

    def sitemap = {
      val entries = List(
        Menu.i("Home") / "index",

        Countries.menu,
        ACountry.menu,
        ARegion.menu,

        L0.menu,
        L1.menu,
        L2.menu,

        Menu.i("A1") / "a1" >> Hidden >> LocGroup("bdd"),
        Menu.i("A2") / "a2" >> Hidden >> LocGroup("bdd"),
        Menu.i("A3") / "a3" >> Hidden >> LocGroup("bdd"),

        Menu.i("Level 1.1.1") / "page111" >> Hidden >> LocGroup("bdd11"),
        Menu.i("Level 1.1.2") / "page112" >> Hidden >> LocGroup("bdd11"),
        Menu.i("Level 1.1.3") / "page113" >> Hidden >> LocGroup("bdd11"),
        Menu.i("Level 1.1.4") / "page114" >> Hidden >> LocGroup("bdd11"),

        Menu.i("Level 1.2.1") / "page121" >> Hidden >> LocGroup("bdd12"),
        Menu.i("Level 1.2.2") / "page122" >> Hidden >> LocGroup("bdd12"),

        Menu.i("Level 1.3") / "page13" >> Hidden >> LocGroup("bdd1"),
        Menu.i("Level 1.4") / "page14" >> Hidden >> LocGroup("bdd1"),
        Menu.i("Level 1.5") / "page15" >> Hidden >> LocGroup("bdd1")
      )

      SiteMap(entries: _*)
    }

    //net.liftweb.builtin.snippet.Surround

    // Dispatch files from old site (all located in specific folder, but must be served at root location)
    LiftRules.dispatch.append(OldSiteDispatch)

    // Set the sitemap.  Note if you don't want access control for each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap))

    // Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(new LoanWrapper { override def apply[T](f: => T): T = inTransaction { f } })

    // Open ID login
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)
    LiftRules.dispatch.append(OpenIdVendor.dispatchPF)
    LiftRules.snippets.append(OpenIdVendor.snippetPF)
  }
}
