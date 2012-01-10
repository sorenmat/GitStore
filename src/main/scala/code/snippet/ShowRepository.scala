package code.snippet

import java.io.File

import scala.Array.canBuildFrom
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.NodeSeq
import scala.xml.Text

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk

import code.helpers.git.GitFileHelper
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

class ShowRepository extends Logger {

	object folder extends RequestVar[String]("")

	def render(template: NodeSeq): NodeSeq = {
		val repoName = S.param("repo").get
		val repodir = ServerSetup.findAll.head.basepath.get
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
