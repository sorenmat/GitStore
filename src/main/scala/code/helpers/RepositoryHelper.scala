package code.helpers
import code.model.ServerSetup
import java.io.File

object RepositoryHelper {

	def findInFileSystem(name: String) = {
		new File(ServerSetup.find().get.basepath)
		""
	}
	def gitRoot = ServerSetup.findAll.head.basepath
	def gitDir(name: String) = new File(gitRoot, name)
	def currentRepository = new File(gitDir(WebSession.repository.get), ".git")

}