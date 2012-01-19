package code.snippet

import java.io.File
import scala.Array.canBuildFrom
import scala.io.Source
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.NodeSeq
import scala.xml.Text
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.treewalk.TreeWalk
import code.helpers.WebSession
import code.model.ServerSetup
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Logger
import net.liftweb.http.SHtml.link
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.mapper.MappedField.mapToType
import net.liftweb.util.Helpers.bind
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import org.gitective.core.BlobUtils

class ShowFile extends Logger {

	object folder extends RequestVar[String]("")

	def render(template: NodeSeq): NodeSeq = {
		val file = S.param("file").get
		val repoName = WebSession.repository.get
		if (repoName == "")
			S.redirectTo("/")

		val repodir = ServerSetup.findAll.head.basepath.get
		WebSession.repository(repoName) // Set repository name on the session

		val builder = new FileRepositoryBuilder()
		val repoFile = new File(repodir, repoName + "/.git")
		println("Repo file: " + repoFile + " " + repoFile.exists())

		val repository = (builder.setGitDir(repoFile)
			.readEnvironment() // scan environment GIT_* variables
			.findGitDir()).build();

		
		val repo = new FileRepository(repoFile);
		val content = "\n"+BlobUtils.getHeadContent(repo, file);
		val html = bind("showfile", template,
			"filecontent" -> {
				<pre class="prettyprint">
				{content.split("\n").map(l => Text(l)++ <br/>).foldLeft(NodeSeq.Empty)((a,b) => a ++ b)}
				</pre>
			})
		repository.close()
		html

	}
}
