package code.snippet
import net.liftweb.http.S
import code.helpers.WebSession
import scala.xml.NodeSeq

trait RepositoryContextPage {

	def repositoryname = WebSession.repository.get
	def render(template: NodeSeq) = {
		if (repositoryname == "")
			S.redirectTo("/")
		content(template)
	}

	def content(template: NodeSeq): NodeSeq
}