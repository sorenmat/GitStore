package code.helpers.git
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.List
import org.eclipse.jgit.api.BlameCommand
import org.eclipse.jgit.blame.BlameResult
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import scala.collection.JavaConversions._

object DiffOutputType extends Enumeration {
	type DiffOutputType = Value
	val PLAIN, GITSTORE = Value
}

object GitDiffHelper {

	def getDiff(repository: Repository, baseCommit: RevCommit, commit: RevCommit, outputType: DiffOutputType.DiffOutputType): String = getDiff(repository, baseCommit, commit, null, outputType);

	/**
	 * Returns the patch between two revisions for the specified file.
	 *
	 * @param repository
	 * @param baseCommit
	 *            The commit to make the diff against otherwise null to use the parent
	 *            the commit.
	 * @param commit
	 * 				The current commit version to be compared with the base
	 * @param path
	 *            Show the patch for this file, otherwise the complete diff is returned
	 * @param outputType
	 * @return 
	 * 			The diff / patch
	 */
	def getDiff(repository: Repository, baseCommit: RevCommit, commit: RevCommit, path: String, outputType: DiffOutputType.DiffOutputType): String = {
		var diff: String = null;
		try {
			val os = new ByteArrayOutputStream();
			val cmp = RawTextComparator.DEFAULT;
			val df = outputType match {
				case DiffOutputType.GITSTORE => 	new GitStoreWeb(os)
				case DiffOutputType.PLAIN => new DiffFormatter(os);
			}
			df.setRepository(repository);
			df.setDiffComparator(cmp);
			df.setDetectRenames(true);

			val commitTree = commit.getTree();
			var baseTree: RevTree = null
			if (baseCommit == null) {
				if (commit.getParentCount() > 0) {
					val rw = new RevWalk(repository);
					val parent = rw.parseCommit(commit.getParent(0).getId());
					rw.dispose();
					baseTree = parent.getTree();
				} else {
					// FIXME initial commit. no parent?!
					baseTree = commitTree;
				}
			} else {
				baseTree = baseCommit.getTree();
			}

			val diffEntries = df.scan(baseTree, commitTree);
			if (path != null && path.length() > 0) {
				var run = true
				for (diffEntry <- diffEntries) {
					if (diffEntry.getNewPath().equalsIgnoreCase(path) && run) {
						df.format(diffEntry);
						run = false
					}
				}
			} else {
				df.format(diffEntries);
			}
			if (df.isInstanceOf[GitWebDiffFormatter]) {
				// workaround for complex private methods in DiffFormatter
				diff = df.asInstanceOf[GitWebDiffFormatter].getHtml()
			} else {
				diff = os.toString();
			}
			df.flush();
		} catch {
			case t: Throwable => println("failed to generate commit diff!", t);
		}
		return diff;
	}
}