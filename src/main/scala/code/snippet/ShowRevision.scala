package code.snippet

import scala.xml.Unparsed
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
import code.helpers.git.GitDiffHelper
import code.helpers.git.DiffOutputType
import code.helpers.WebSession
import code.helpers.RepositoryHelper._

class ShowRevision extends Logging {

	def render(template: NodeSeq): NodeSeq = {
		val revision = S.param("rev")
		val builder = new FileRepositoryBuilder();
		
		
		val repo = builder.setGitDir(currentRepository)
			.readEnvironment() // scan environment GIT_* variables
			.findGitDir().build();

		val git = new Git(repo)

		val lastCommitId = repo.resolve(revision.get)
		// retrieve the tree in HEAD
		val revWalk = new RevWalk(repo);
		val commit = revWalk.parseCommit(lastCommitId);

		val sb = new StringBuilder()
		sb.append(GitDiffHelper.getDiff(repo, null, commit, DiffOutputType.GITSTORE))
//		println("********* PATCH **************")
//		println(sb.toString())
//		println("********* PATCH **************")
		Unparsed(sb.toString())

	}
}



