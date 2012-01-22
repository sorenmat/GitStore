package code.snippet

import scala.xml.NodeSeq
import scala.xml.Unparsed
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import code.helpers.RepositoryHelper.currentRepository
import code.helpers.git.DiffOutputType
import code.helpers.git.GitDiffHelper
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Logger
import net.liftweb.http.S
import org.gitective.core.BlobUtils
import org.eclipse.jgit.storage.file.FileRepository

class ShowRevision extends Logger {

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

		println("currentRepo "+currentRepository.getAbsolutePath())
		val myrepo = new FileRepository(currentRepository.getAbsolutePath())

		
		val current = BlobUtils.getId(repo, "HEAD", "build.sbt");
		val previous = BlobUtils.getId(repo, "HEAD^4", "build.sbt");

		info("current %s, previous".format(current, previous))
		println("current %s, previous".format(current, previous))
		val edit = BlobUtils.diff(repo, previous, current);
		println("PATCH\n\n" + edit + "\n\nPATCH");
		//		println("********* PATCH **************")
		Unparsed(sb.toString())

	}
}



