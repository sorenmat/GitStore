package code.helpers.git
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.revwalk.RevCommit
import java.io.IOException
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.Constants

object GitFileHelper {

	def getFilesInPath(repository: Repository, path: String, revision: RevCommit) = {
		var list = List[FileModel]()

		val tw = new TreeWalk(repository)
		try {
			tw.addTree(revision.getTree())
			if (!path.isEmpty()) {
				val f = PathFilter.create(path)
				var foundDirectory = false

				tw.setFilter(f)
				tw.setRecursive(false)

				while (tw.next()) {
					if (!foundDirectory && tw.isSubtree()) {
						tw.enterSubtree()
					}
					if (tw.getPathString().equals(path)) {
						foundDirectory = true
					} else if (foundDirectory) {
						list = getFileModel(tw, path, revision) :: list
					}
				}
			} else {
				tw.setRecursive(false)
				while (tw.next()) {
					list = getFileModel(tw, null, revision) :: list
				}
			}
		} catch {
			case e: IOException => println("Failed to get files for commit " + revision.getName())
		} finally {
			tw.release()
		}
		list
	}

	def getFileModel(tw: TreeWalk, basePath: String, revision: RevCommit) = {
		var size = 0L
		var name = if (basePath == null || basePath.isEmpty())
			tw.getPathString()
		else
			tw.getPathString().substring(basePath.length() + 1)

		try {
			if (!tw.isSubtree()) {
				size = tw.getObjectReader().getObjectSize(tw.getObjectId(0), Constants.OBJ_BLOB)
			}
		} catch {
			case t: Throwable => println("failed to retrieve size for file " + tw.getPathString())
		}
		new FileModel(name, tw.getPathString(), size, tw.getFileMode(0).getBits(), tw.isSubtree(), revision.getName())
	}

	case class FileModel(name: String, path: String, size: Long, mode: Int, isDirectory: Boolean, commitId: String)
}
