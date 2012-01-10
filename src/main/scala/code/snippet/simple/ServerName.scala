package code.snippet.simple
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.sitemap.SiteMap
import code.model.ServerSetup

class ServerName {
	def name(in: NodeSeq): NodeSeq = Text(ServerSetup.findAll.head.name.get)
}