package code.snippet.simple
import scala.xml.NodeSeq
import scala.xml.Text
import com.gitstore.auth.LDAPUtil
import code.helpers.WebSession
import code.model.ServerSetup
import code.model.User
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Box.option2Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.SHtml.password
import net.liftweb.http.SHtml.submit
import net.liftweb.http.SHtml.text
import net.liftweb.mapper.By
import net.liftweb.util.Helpers.bind
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import com.mongodb.BasicDBObject
import net.liftweb.common.Failure
import code.helpers.UserHelper

class Login {
	def render(in: NodeSeq): NodeSeq = {
		var username = ""
		var passwd = ""
		WebSession.loggedInUser.is match {
			case Full(x) =>
				bind("login", in,
					"username" -%> Text("welcome " + WebSession.loggedInUser.get.get.username),
					"password" -> Text(""),
					"submit" -%> submit("Logout", () => WebSession.loggedInUser(None)))
			case Empty => bind("login", in,
				"username" -%> text("username", text => username = text),
				"password" -%> password("password", text => passwd = text),
				"submit" -%> submit("Login", () => {
					val user = UserHelper.getUser(username, passwd)
					WebSession.loggedInUser(Full(user))
					println("user " + user.username + " logged in")
				}))

			case Failure(_, _, _) => NodeSeq.Empty

		}
	}

}