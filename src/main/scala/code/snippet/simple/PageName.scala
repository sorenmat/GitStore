package code.snippet.simple
import net.liftweb.http.S
import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.sitemap.SiteMap

class PageName {
	def name(in: NodeSeq): NodeSeq = S.location.get.title
}