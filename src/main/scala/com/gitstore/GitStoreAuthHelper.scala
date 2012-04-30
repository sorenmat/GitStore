package com.gitstore
import java.nio.charset.Charset

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException
import org.eclipse.jgit.util.Base64

import com.gitstore.auth.LDAPUtil
import com.mongodb.BasicDBObject

import code.model.User
import javax.servlet.http.HttpServletRequest
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import net.liftweb.common.Full
import net.liftweb.common.Loggable

object GitStoreAuthHelper extends Loggable {
	val BASIC = "Basic"
	val CHALLENGE = "Basic" + " realm=\"Gitstore\""

	/**
	 *
	 * Check if a user has access to repository.
	 * If the server is public all checks are disabled.
	 *
	 */
	def checkAuth(req: HttpServletRequest, db: Repository, writeAccess: Boolean): Boolean = {
		try {
			//			val public = ServerSetup.findAll.head.public.get
			//			if(public)
			//				return true
			getUsernameAndPasswordFromRequest(req) match {
				case Some(x) => {
					val username = x._1
					val password = x._2
					val authenticated = LDAPUtil.authenticate(username, password)
					if (!authenticated) {
						println("User '" + username + "' not authenticated !")
						throw new ServiceNotAuthorizedException()
					}
					logger.info("user '" + username + "' tried to login")
					val user = User.find(new BasicDBObject("username", username))
					user match {
						case Full(u) => {
							logger.info("User '" + username + "' found in database, checking access")
							return checkUserAccessToRepository(u, db, writeAccess)

						}
						case Empty => {
							logger.info(username + " NOT found in database, creating it now.")
							val user = User.createRecord.username(username)
							user.save
							return checkUserAccessToRepository(user, db, writeAccess)
						}
						case Failure(_, _, _) =>
							throw new ServiceNotAuthorizedException()
					}

				}
				case None => {
					return false

				}
			}
		} catch {
			case e: Exception => {
				e.printStackTrace();
				return false
			}
		}
	}

	/**
	 *
	 * Check to see if the user has all the groups specified in the repository setting.
	 *
	 */
	def checkUserAccessToRepository(user: User, db: Repository, writeAccess: Boolean): Boolean = {
		logger.info("Direcotry name: " + db.getDirectory().getParentFile().getName())
		val repositoryName = db.getDirectory().getParentFile().getName()
		val repoOption = code.model.Repository.find(new BasicDBObject("name", repositoryName))
		repoOption match {
			case Full(repo) =>
				val userGroups = LDAPUtil.getGroups(user.username.get).toSet

				if (writeAccess) {
					if (repo.read_write_groups.get.toSet.subsetOf(userGroups)) {
						logger.debug("User has access to repo")
						return true
					}
				}
				if (!writeAccess) {
					if (repo.read_groups.get.toSet.subsetOf(userGroups)) {
						logger.debug("User has access to repo")
						return true
					}
				}
				throw new ServiceNotAuthorizedException()
			//				
			//				if (!repo.read_write_groups.get.toSet.subsetOf(userGroups)) {
			//					logger.info("User '" + user.username.toString() + " 'hasn't access to the repository")
			//					throw new ServiceNotAuthorizedException()
			//				} else {
			//					if (!writeAccess) {
			//						if (!repo.read_groups.get.toSet.subsetOf(userGroups)) {
			//							logger.info("User '" + user.username.toString() + " 'hasn't access to the repository")
			//							throw new ServiceNotAuthorizedException()
			//						}
			//					}
			//
			//				}

			case Empty => {
				throw new ServiceNotAuthorizedException()
			}
			case Failure(_, _, _) =>
				throw new ServiceNotAuthorizedException()
		}
	}

	/**
	 *
	 * Get the username and password from the http request using the Authorization header value
	 * Assumes that the values are stored in UTF-8
	 *
	 * @return Option[(Username, Password)]
	 *
	 */
	def getUsernameAndPasswordFromRequest(req: HttpServletRequest) = {
		val authorization = req.getHeader("Authorization")
		if (authorization != null && authorization.startsWith(BASIC)) {
			val base64Credentials = authorization.substring(BASIC.length()).trim()
			val credentials = new String(Base64.decode(base64Credentials), Charset.forName("UTF-8"))
			val values = credentials.split(":")

			val username = values(0)
			val password = values(1)
			Some((username, password))
		} else None
	}
}