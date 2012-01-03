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
					val user = getUser(username, passwd)
					if (user.isEmpty) {
						println("woring username or password ")
					} else {
						val u = user.head
						WebSession.loggedInUser(Full(u))
						println("user " + u.username + " logged in")
					}
				}))

		}
	}
	def getUser(username: String, passwd: String) = {
		println("is ldap enabled " + ServerSetup.instance.ldap_enabled.get)
		if (ServerSetup.instance.ldap_enabled.get) {
			val authenticated = LDAPUtil.search(username, passwd, Nil)
			println("User '" + username + "' was authenticated")
			val users = User.findAll(By(User.username, username))
			users.head.password(passwd).save // password should not be in plain text
			users
		} else
			User.findAll(By(User.username, username), By(User.password, passwd))
	}
}