package code.snippet

import scala.collection.JavaConversions._
import scala.xml._
import scala.xml._
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.Text
import org.eclipse.jgit.api._
import org.eclipse.jgit.storage.file._
import code.helpers.WebSession
import net.liftweb.http.SHtml._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import net.liftweb.http._
import net.liftweb.http._
import net.liftweb.http._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.util._
import net.liftweb.widgets.gravatar.Gravatar
import net.liftweb._
import code.helpers.RepositoryHelper
import net.liftweb.common.Logger

class ShowCommits extends Logger {

	def myrender(template: NodeSeq): NodeSeq = {
		val repoName = WebSession.repository.get
		val builder = new FileRepositoryBuilder()
		val repository = builder.setGitDir(RepositoryHelper.currentRepository)
			.readEnvironment() // scan environment GIT_* variables
			.findGitDir().build();

		val git = new Git(repository)

		git.log().call().flatMap(c => {
			bind("log", template,
				"rev" -> c.getId().name(),
				"commiterPicture" -> Gravatar(c.getCommitterIdent().getEmailAddress(), 24),
				"commiter" -> c.getCommitterIdent().getName(),
				"comment" -> link("/showrev?rev=" + c.getId().getName(), () => {}, Text(c.getFullMessage())),
				"date" -> new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(c.getCommitterIdent().getWhen()))
		}).toSeq
	}

	def clickableImage(xhtml: NodeSeq, url: String, image: String, title: String): NodeSeq = {
		<a href={ url } title={ title }><img src={ image } height="16" width="16"/></a>
	}

}
