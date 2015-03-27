package models

import java.net.URL
import scala.xml._
import scala.util.Try

case class ProfileSubItem(description: String, url: String)

case class ProfileItem(profileSubItems: Seq[ProfileSubItem])

case class ProfileList(title: String, profileItems: Seq[ProfileItem])

case class Profile(introduction: String, profileLists: Seq[ProfileList]) {
  def partOfProfileLists(partNumber: Int, partCount: Int): Seq[ProfileList] = {
    val totalItemCount = profileLists.map(_.profileItems.size).sum

    // @todo
    profileLists
  }
}

object Profile {
  def apply(url: URL): Try[Profile] = for {
    xml <- Try(XML.load(url))
    profile <- apply(xml)
  } yield profile

  def apply(elem: Elem): Try[Profile] = Try {
    val lists = (elem \\ "lists").head
    val introduction = (lists \\ "introduction").head.text

    val profileLists = for {
      list <- lists \\ "list"
      title = list \@ "title"
    } yield {
        val profileItems = for (item <- list \\ "item") yield {
          val profileSubItems = for {
            subitem <- item \\ "subitem"
            description = subitem.text
            url = subitem \@ "url"
          } yield ProfileSubItem(description, url)

          val allProfileSubItems = if (profileSubItems.nonEmpty)
            profileSubItems
          else
            Seq(ProfileSubItem(description = item.text, url = item \@ "url"))

          ProfileItem(allProfileSubItems)
        }

      ProfileList(title, profileItems)
    }

    Profile(introduction, profileLists)
  }
}
