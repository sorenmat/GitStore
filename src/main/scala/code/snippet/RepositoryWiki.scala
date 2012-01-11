package code.snippet
import scala.xml.NodeSeq
import com.gitstore.auth.GroupHelper
import com.gitstore.auth.LDAPUtil
import com.mongodb.BasicDBObject
import code.helpers.WebSession
import code.model.Repository
import net.liftweb.common.Box.box2Option
import net.liftweb.http.SHtml.multiSelect
import net.liftweb.http.SHtml.submit
import net.liftweb.http.S
import net.liftweb.util.Helpers.bind
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.widgets.autocomplete.AutoComplete
import net.liftweb.http.SHtml
import com.petebevin.markdown.MarkdownProcessor
import net.liftweb.http.js.JsCmds._
import scala.xml.Text
import net.liftweb.http.js.JsCmd
import net.liftweb.http.SHtml.ElemAttr
import scala.xml.XML
class RepositoryWiki {

	def render(form: NodeSeq) = {
		val repoName = WebSession.repository.get
		if (repoName == "")
			S.redirectTo("/")

		var text = ""

		def preview(): JsCmd = {
			val m = new MarkdownProcessor()
			val html = m.markdown(text)
			SetHtml("wikiresult", XML.loadString(html))

		}
		def checkAndSave(): JsCmd = {
			println("clicked !!")
			val m = new MarkdownProcessor()
			val html = m.markdown(text)
			println(text + " was html is " + html)
			SetHtml("wikiresult", XML.loadString(html))

		}
		def doBind(form: NodeSeq) = {
			bind("wiki", form,
				"textfield" -> SHtml.ajaxTextarea("", s => {
					println("--->" + s)
					text = s
				}),
				"preview" -> SHtml.ajaxButton("Update", () => preview),
				"save" -> SHtml.ajaxButton("Update", () => checkAndSave))
		}

		doBind(form)
	}

}