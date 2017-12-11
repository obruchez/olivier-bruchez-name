package models

import java.net.URL
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Contact(name: String, url: String, urlDescription: String, icon: String)

object Contact {
  def apply(rootNode: Node): Try[Contact] = Try {
    val urlNode = rootNode \\ "url"
    Contact(name = (rootNode \\ "name").text.trim,
            url = urlNode.text.trim,
            urlDescription = urlNode \@ "description",
            icon = (rootNode \\ "icon").text.trim)
  }
}

case class Contacts(override val introduction: Option[Introduction], contacts: Seq[Contact])
    extends Cacheable

object Contacts extends Fetchable {
  type C = Contacts

  override val name = "Contacts"
  override val sourceUrl = Configuration.baseUrlWithFile("contacts.xml").get
  override val icon = Some("fa-envelope-o")

  override def fetch(): Try[Contacts] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Contacts] =
    for {
      xml <- Try(XML.load(url))
      contacts <- apply(xml)
    } yield contacts

  def apply(rootNode: Node): Try[Contacts] = Try {
    val contactsNode = (rootNode \\ "contacts").head
    val introduction = Parsing.introductionFromNode(contactsNode).get
    val contactsSeq = (contactsNode \\ "contact").map(Contact(_).get)

    Contacts(introduction, contactsSeq)
  }
}
