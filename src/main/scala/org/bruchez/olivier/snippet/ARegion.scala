package org.bruchez.olivier.snippet

import org.bruchez.olivier.model._

import net.liftweb._
import common._
import util._
import Helpers._
import sitemap._
import scala.xml.Text

object ARegion extends Loggable {
  logger.info("snippet.ARegion obj start")
  
  lazy val menu = {
    Menu.params[(Country, Region)]("Region", Loc.LinkText(r => Text( (r._2.name)._1) ),
                                  {
                                    case Country(cid) ::
                                    Region(rid) :: _ =>
                                       Full((cid, rid))
                                    case _ => Empty
                                  },
                                  cr => List(cr._1.seoUrl.toString,
                                             cr._2.seoUrl.toString)
                                ) / "countries" / * / *
  }
  
  logger.info("snippet.ARegion obj end")
}

class ARegion(cr: (Country, Region)) extends Loggable {
  logger.info("ARegion obj county.id="+cr._1.id+" region.id="+cr._2.id+" start")
  def render = { <div>Current Country: <b>{cr._1.name}</b> Current Region: <b>{cr._2.name}</b></div> }
  logger.info("ARegion obj end")
}
