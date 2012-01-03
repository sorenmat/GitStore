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

	/**
	 * Returns the complete diff of the specified commit compared to its primary
	 * parent.
	 *
	 * @param repository
	 * @param commit
	 * @param outputType
	 * @return the diff as a string
	 */
	def getCommitDiff(repository: Repository, commit: RevCommit, outputType: DiffOutputType.DiffOutputType) = getDiff(repository, null, commit, null, outputType);

	/**
	 * Returns the diff for the specified file or folder from the specified
	 * commit compared to its primary parent.
	 *
	 * @param repository
	 * @param commit
	 * @param path
	 * @param outputType
	 * @return the diff as a string
	 */
	def getDiff(repository: Repository, commit: RevCommit, path: String, outputType: DiffOutputType.DiffOutputType): String = getDiff(repository, null, commit, path, outputType);

	/**
	 * Returns the complete diff between the two specified commits.
	 *
	 * @param repository
	 * @param baseCommit
	 * @param commit
	 * @param outputType
	 * @return the diff as a string
	 */
	def getDiff(repository: Repository, baseCommit: RevCommit, commit: RevCommit, outputType: DiffOutputType.DiffOutputType): String = getDiff(repository, baseCommit, commit, null, outputType);

	/**
	 * Returns the diff between two commits for the specified file.
	 *
	 * @param repository
	 * @param baseCommit
	 *            if base commit is null the diff is to the primary parent of
	 *            the commit.
	 * @param commit
	 * @param path
	 *            if the path is specified, the diff is restricted to that file
	 *            or folder. if unspecified, the diff is for the entire commit.
	 * @param outputType
	 * @return the diff as a string
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
				diff = df.asInstanceOf[GitWebDiffFormatter].getHtml();
			} else {
				diff = os.toString();
			}
			df.flush();
		} catch {
			case t: Throwable => println("failed to generate commit diff!", t);
		}
		return diff;
	}

	/**
	 * Returns the diff between the two commits for the specified file or folder
	 * formatted as a patch.
	 *
	 * @param repository
	 * @param baseCommit
	 *            if base commit is unspecified, the patch is generated against
	 *            the primary parent of the specified commit.
	 * @param commit
	 * @param path
	 *            if path is specified, the patch is generated only for the
	 *            specified file or folder. if unspecified, the patch is
	 *            generated for the entire diff between the two commits.
	 * @return patch as a string
	 */
	def getCommitPatch(repository: Repository, baseCommit: RevCommit, commit: RevCommit, path: String): String = {
		var diff: String = null;
		try {
			val os = new ByteArrayOutputStream();
			val cmp = RawTextComparator.DEFAULT;
			val df = new PatchFormatter(os);
			df.setRepository(repository);
			df.setDiffComparator(cmp);
			df.setDetectRenames(true);

			val commitTree = commit.getTree();
			var baseTree: RevTree = null
			if (baseCommit == null) {
				if (commit.getParentCount() > 0) {
					val rw = new RevWalk(repository);
					val parent = rw.parseCommit(commit.getParent(0).getId());
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
			diff = df.getPatch(commit);
			df.flush();
		} catch {
			case t: Throwable => println("failed to generate commit diff!", t); t.printStackTrace()
		}
		return diff;
	}

	
}