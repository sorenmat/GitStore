import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import java.io.File
import scala.collection.JavaConversions._
import code.model.User
import code.model.Group

object UsersAndGroupsTest {

	def main(args: Array[String]) {
		val group = Group.create.groupname("Main Test group")
		group.save
		
//		User.create.username("tester").email("test@test.com").password("test").groups(group.id)

	}
}