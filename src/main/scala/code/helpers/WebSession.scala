package code.helpers
import net.liftweb.http.SessionVar
import code.model.User
import net.liftweb.common.Empty
import net.liftweb.common.Box

object WebSession {
	object repository extends SessionVar[String]("")
	object loggedInUser extends SessionVar[Box[User]](None)
	
	def getCurrentUser {
		println("Current user called")
	}
}