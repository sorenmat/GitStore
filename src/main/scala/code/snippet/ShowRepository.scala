package code.snippet

import net.liftweb._
import net.liftweb.http._
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import scala.xml._
import SHtml._
import code.model._
import mapper._
import _root_.scala.xml.Text
import java.io.File
import scala.xml.{ NodeSeq, Text }
import net.liftweb._
import http._
import js._
import JsCmds._
import common._
import util._
import Helpers._
import com.schantz.scala.Logging
import _root_.scala.xml._
import http._
import S._
import SHtml._
import common._
import util._
import Helpers._
import js._
import JsCmds._
import auth._
import com.mongodb._
import net.liftweb.json.JsonDSL._
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file._
import org.eclipse.jgit.api._
import scala.collection.JavaConversions._
import net.liftweb.widgets.gravatar.Gravatar
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.eclipse.jgit.revwalk.RevTree
import code.helpers.git.GitFileHelper
import code.helpers.WebSession

class ShowRepository extends Logging {

	object folder extends RequestVar[String]("")

	def render(template: NodeSeq): NodeSeq = {
		val repoName = S.param("repo").get
		val repodir = ServerSetup.findAll.head.basepath
		WebSession.repository(repoName) // Set repository name on the session

		println("Folder = " + folder)
		val builder = new FileRepositoryBuilder();
		val repoFile = new File(repodir, repoName + "/.git")
		println("Repo file: " + repoFile + " " + repoFile.exists())
		val repo = (builder.setGitDir(repoFile)
			.readEnvironment() // scan environment GIT_* variables
			.findGitDir()).build();

		val git = new Git(repo)
		val lastCommitId = repo.resolve(Constants.HEAD)
		// retrieve the tree in HEAD
		val revWalk = new RevWalk(repo);
		val commit = revWalk.parseCommit(lastCommitId);

		// and using commit's tree find the path
		val tree = commit.getTree();

		val treeWalk = new TreeWalk(repo)
		treeWalk.addTree(tree);
		var run = true

		val fileItems = GitFileHelper.getFilesInPath(repo, folder.get, commit)

		val html = fileItems.sortWith((item, item1) => item.isDirectory > item1.isDirectory).map(item =>
			bind("filetree", template,
				"icon" -> {
					if (item.isDirectory) clickableImage("", "images/dir.png", "") else clickableImage("", "images/txt.png", "")
				},
				"filename" -> link(S.uri + "?repo=" + repoName, () => {
					folder(item.path)
				}, Text(item.name)))).flatMap(f => f)
		repo.close()
		html

	}
	def clickableImage(url: String, image: String, title: String): NodeSeq = {
		<a href={ url } title={ title }><img src={ image } height="16" width="16"/></a>
	}

	def breadcrumb: NodeSeq = {
		val paths = folder.get.split("/")
		val html = paths.map(path => <li>{ link(S.uri + "?repo=" + WebSession.repository.get, () => folder(path), Text(path)) }<span class="divider">/</span></li>)
		html.toSeq
	}
}
