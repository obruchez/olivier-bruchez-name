package models

import java.net.URL
import scala.xml._
import scala.util.Try
import util.{Configuration, HtmlContent}

case class ProfileSubItem(description: String, url: String)

case class ProfileItem(profileSubItems: Seq[ProfileSubItem])

case class ProfileList(title: String, profileItems: Seq[ProfileItem], slug: String)

case class Profile(override val introduction: HtmlContent, profileLists: Seq[ProfileList]) extends Cacheable {
  override val size = profileLists.size

  /**
   * @param partNumber 0-based part number (maximum value can be partCount - 1)
   * @param partCount total number of parts
   * @return a subset of the ProfileList instances corresponding to the (partNumber + 1)-th part when dividing all the
   *         ProfileList instances into partCount parts
   */
  def partOfProfileLists(partNumber: Int, partCount: Int): Seq[ProfileList] = {
    val totalProfileItemCount = profileLists.map(_.profileItems.size).sum

    var startInColumns = 0.0

    val profileListEndsInColumns = for ((profileList, profileListIndex) <- profileLists.zipWithIndex) yield {
      val profileListLengthInColumns =
        partCount * profileList.profileItems.size.toDouble / totalProfileItemCount.toDouble

      val pair = profileListIndex -> startInColumns

      startInColumns += profileListLengthInColumns

      pair
    }

    for {
      (index, startInColumns) <- profileListEndsInColumns
      if startInColumns >= partNumber
      if startInColumns < partNumber + 1
      profileList = profileLists.apply(index)
    } yield profileList
  }
}

object Profile extends Fetchable {
  type C = Profile

  override val name = "About / profile"
  override val sourceUrl = Configuration.url("url.profile").get

  override def fetch(): Try[Profile] = apply(sourceUrl)

  def apply(url: URL): Try[Profile] = for {
    xml <- Try(XML.load(url))
    profile <- apply(xml)
  } yield profile

  def apply(elem: Elem): Try[Profile] = Try {
    val profile = (elem \\ "profile").head
    val introduction = Lists.introductionFromNode(profile).get

    val profileLists = for {
      list <- profile \\ "list"
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

      ProfileList(title, profileItems, slug = Lists.slugFromString(title))
    }

    Profile(introduction, profileLists)
  }
}
