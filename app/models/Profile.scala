package models

import java.net.URL
import scala.xml._
import scala.util.Try

case class ProfileItem(description: String, url: String)

case class ProfileList(title: String, profileItems: Seq[ProfileItem])

case class Profile(profileLists: Seq[ProfileList])

object Profile {
  def apply(url: URL): Try[Profile] = for {
    xml <- Try(XML.load(url))
    profile <- apply(xml)
  } yield profile

  def apply(elem: Elem): Try[Profile] = Try {
    val profileLists = for {
      lists <- elem \\ "lists"
      list <- lists \\ "list"
      title = list \@ "title"
    } yield {
      val profileItems = for {
        item <- list \\ "item"
        description = item.text
        url = item \@ "url"
      } yield ProfileItem(description, url)

      ProfileList(title, profileItems)
    }

    Profile(profileLists)
  }
}
