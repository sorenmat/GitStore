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

object GitStoreAuthHelper extends Loggable {
	val BASIC = "Basic"
	val CHALLENGE = "Basic" + " realm=\"Gitstore\""

	def checkAuth(req: HttpServletRequest, resp: HttpServletResponse, db: Repository) {
		val authorization = req.getHeader("Authorization")
		if (authorization != null && authorization.startsWith(BASIC)) {
			val base64Credentials = authorization.substring(BASIC.length()).trim()
			val credentials = new String(Base64.decode(base64Credentials), Charset.forName("UTF-8"))
			val values = credentials.split(":")

			if (values.length == 2) {
				val username = values(0)
				val password = values(1)
				logger.info("user '" + username + "' tried to login")
				val user = User.find(By(User.username, username), By(User.password, password))
				user match {
					case Full(u) => checkUserAccessToRepository(u, db)
					case Empty => {
						throw new ServiceNotAuthorizedException()
					}
					case Failure(_, _, _) =>
						throw new ServiceNotAuthorizedException()
				}
			}
		} else {
			resp.setHeader("WWW-Authenticate", CHALLENGE)
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
			throw new ServiceNotAuthorizedException()
		}
	}

	def checkUserAccessToRepository(user: User, db: Repository) {
		logger.debug("Direcotry name: " + db.getDirectory().getParentFile().getName())
		val repositoryName = db.getDirectory().getParentFile().getName()
		val repo = code.model.Repository.find(By(code.model.Repository.name, repositoryName))
		repo match {
			case Full(u) =>
				if (u.users.find(repoUser => repoUser.username.toString == user.username.toString()).isEmpty) {
					// is the repos groups all in the user group list
					if (!u.groups.toSet.subsetOf(user.groups.toSet))
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