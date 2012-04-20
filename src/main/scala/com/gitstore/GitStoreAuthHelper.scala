package com.gitstore
import javax.servlet.http.HttpServletRequest
import java.nio.charset.Charset
import net.liftweb.common.Full
import net.liftweb.common.Failure
import net.liftweb.common.Empty
import code.model.User
import org.eclipse.jgit.util.Base64
import net.liftweb.mapper.By
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException
import org.eclipse.jgit.lib.Repository
import javax.servlet.http.HttpServletResponse
import net.liftweb.common.Loggable
import code.helpers.LDAPHelper
import com.gitstore.auth.LDAPUtil
import com.mongodb.BasicDBObject

object GitStoreAuthHelper extends Loggable {
	val BASIC = "Basic"
	val CHALLENGE = "Basic" + " realm=\"Gitstore\""

	def checkAuth(req: HttpServletRequest, resp: HttpServletResponse, db: Repository) {
		try {

			val authorization = req.getHeader("Authorization")
			if (authorization != null && authorization.startsWith(BASIC)) {
				val base64Credentials = authorization.substring(BASIC.length()).trim()
				val credentials = new String(Base64.decode(base64Credentials), Charset.forName("UTF-8"))
				val values = credentials.split(":")

				if (values.length == 2) {
					val username = values(0)
					val password = values(1)
					val authenticated = LDAPUtil.authenticate(username, password)
					if (!authenticated) {
						println("User '" + username + "' not authenticated !")
						throw new ServiceNotAuthorizedException()
					}
					logger.info("user '" + username + "' tried to login")
					val user = User.find(new BasicDBObject("username", username))
					user match {
						case Full(u) => checkUserAccessToRepository(u, db)
						case Empty => {
							logger.info(username+" not found in database, creating it now.")
							val user = User.createRecord.username(username)
							user.save
							checkUserAccessToRepository(user, db)
						}
						case Failure(_, _, _) =>
							throw new ServiceNotAuthorizedException()
					}
				}
			} else {
				resp.setHeader("WWW-Authenticate", CHALLENGE)
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
			}
		} catch {
			case e: Exception => e.printStackTrace(); throw new ServiceNotAuthorizedException()
		}
	}

	def checkUserAccessToRepository(user: User, db: Repository) {
		logger.info("Direcotry name: " + db.getDirectory().getParentFile().getName())
		val repositoryName = db.getDirectory().getParentFile().getName()
		val repoOption = code.model.Repository.find(new BasicDBObject("name", repositoryName))
		repoOption match {
			case Full(repo) =>
				val userGroups = LDAPUtil.getGroups(user.username.get).toSet
				
				if (!repo.groups.get.toSet.subsetOf(userGroups)) {
					logger.info("User '"+user.username.toString()+" 'hasn't access to the repository")
					throw new ServiceNotAuthorizedException()
					
				}
				logger.debug("User has access to repo")
			case Empty => {
				throw new ServiceNotAuthorizedException()
			}
			case Failure(_, _, _) =>
				throw new ServiceNotAuthorizedException()
		}
	}
}