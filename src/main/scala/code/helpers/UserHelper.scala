package code.helpers
import code.model.User
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException
import com.mongodb.BasicDBObject
import java.util.logging.Logging
import net.liftweb.common.Full
import net.liftweb.common.Failure
import net.liftweb.common.Empty
import net.liftweb.common.Loggable
import com.gitstore.auth.LDAPUtil

object UserHelper extends Loggable {

	def getUser(username: String, password: String) : User = {
		val authenticated = LDAPUtil.authenticate(username, password)
		val user = User.find(new BasicDBObject("username", username))
		user match {
			case Full(u) => {
				logger.info("User '" + username + "' found in database, checking access")
				return u

			}
			case Empty => {
				logger.info(username + " NOT found in database, creating it now.")
				val user = User.createRecord.username(username)
				user.save
				return user
			}
			case Failure(_, _, _) =>
				throw new RuntimeException("Error retrieving or creating user")
		}
	}

}