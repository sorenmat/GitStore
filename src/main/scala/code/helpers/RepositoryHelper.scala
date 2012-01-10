package code.helpers
import code.model.ServerSetup
import java.io.File

object RepositoryHelper {

	def findInFileSystem(name: String) = {
		new File(ServerSetup.instance.basepath.get)
		""
	}
	def gitRoot = ServerSetup.findAll.head.basepath.get
	def gitDir(name: String) = new File(gitRoot, name)
	def currentRepository = new File(gitDir(WebSession.repository.get), ".git")

}