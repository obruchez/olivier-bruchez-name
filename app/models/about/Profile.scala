package models.about

import java.net.URL
import models.{Cacheable, Fetchable, Introduction}
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class ProfileSubItem(description: String, url: String)

object ProfileSubItem {
  def apply(rootNode: Node): Try[ProfileSubItem] = Try {
    ProfileSubItem(description = rootNode.text, url = rootNode \@ "url")
  }
}

case class ProfileItem(profileSubItems: Seq[ProfileSubItem])

object ProfileItem {
  def apply(rootNode: Node): Try[ProfileItem] = Try {
    val profileSubItemsSeq = (rootNode \\ "subitem").map(ProfileSubItem(_).get)

    val allProfileSubItemsSeq =
      if (profileSubItemsSeq.nonEmpty)
        profileSubItemsSeq
      else
        Seq(ProfileSubItem(description = rootNode.text, url = rootNode \@ "url"))

    ProfileItem(allProfileSubItemsSeq)
  }
}

case class ProfileList(title: String, profileItems: Seq[ProfileItem], slug: String)

object ProfileList {
  def apply(rootNode: Node): Try[ProfileList] = Try {
    val title = rootNode \@ "title"
    val profileItemsSeq = (rootNode \\ "item").map(ProfileItem(_).get)

    ProfileList(title, profileItemsSeq, slug = Slug.slugFromString(title))
  }
}

case class Profile(override val introduction: Option[Introduction], profileLists: Seq[ProfileList])
    extends Cacheable {

  /**
    * @param partNumber 0-based part number (maximum value can be partCount - 1)
    * @param partCount total number of parts
    * @return a subset of the ProfileList instances corresponding to the (partNumber + 1)-th part when dividing all the
    *         ProfileList instances into partCount parts
    */
  def partOfProfileLists(partNumber: Int, partCount: Int): Seq[ProfileList] = {
    val totalProfileItemCount = profileLists.map(_.profileItems.size).sum

    var startInColumns = 0.0

    val profileListEndsInColumns =
      for ((profileList, profileListIndex) <- profileLists.zipWithIndex) yield {
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

  override val name = "Profile"
  override val sourceUrl = Configuration.baseUrlWithFile("profile.xml").get
  override val icon = Some("fa-list")

  override def fetch(): Try[Profile] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Profile] =
    for {
      xml <- Try(XML.load(url))
      profile <- apply(xml)
    } yield profile

  def apply(rootNode: Node): Try[Profile] = Try {
    val profileNode = (rootNode \\ "profile").head
    val introduction = Parsing.introductionFromNode(profileNode).get
    val profileListsSeq = (rootNode \\ "list").map(ProfileList(_).get)

    Profile(introduction, profileListsSeq)
  }
}
