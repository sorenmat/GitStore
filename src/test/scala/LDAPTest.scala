import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import java.io.File
import scala.collection.JavaConversions._
import code.model.User
import com.gitstore.auth.LDAPUtil
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.DefaultMongoIdentifier
import com.mongodb.Mongo

object LDAPTest {

	def main(args: Array[String]) {
		MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "GitStore")
		println(LDAPUtil.getGroups)
		println(LDAPUtil.getUsers)
	}
}