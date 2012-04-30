package com.gitstore

import org.eclipse.jgit.http.server.GitServlet
import javax.servlet.ServletConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.Charset
import org.eclipse.jgit.util.Base64
import code.model.User
import net.liftweb.mapper.By
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import org.eclipse.jgit.lib.RepositoryCache
import java.io.File
import org.eclipse.jgit.lib.RepositoryCache.FileKey
import org.eclipse.jgit.util.FS
import code.model.ServerSetup
import net.liftweb.common.Logger
import javax.servlet.FilterConfig

class GitStoreServlet extends GitServlet with Logger {
	val BASIC = "Basic"
	val CHALLENGE = "Basic" + " realm=\"Gitstore\""

	def configMap = Map("base-path" -> ServerSetup.findAll.head.basepath.toString, "export-all" -> "true")

	override def init(config: ServletConfig) {
		try {
			info("Starting git store servlet..")

			setReceivePackFactory(new GitStoreReceivePackFactory())
			setUploadPackFactory(new GitStoreUploadPackFactory())

			//			super.init(config)
			super.init(new ServletConfig() {
				override def getServletName(): String = this.getClass().getName()
				override def getInitParameter(name: String) = {
					println("Trying to get init parm name " + name)
					//					config.getInitParameter(name)
					configMap(name)
				}

				override def getInitParameterNames = config.getInitParameterNames()

				override def getServletContext = config.getServletContext()
			});
		} catch {
			case t: Throwable => {
				t.printStackTrace()
				error(t)
			}
		}
	}

	override def service(req: HttpServletRequest, resp: HttpServletResponse) {
		val basePath = ServerSetup.findAll.head.basepath.toString
		info("BasePath: " + basePath)
		val repositoryName = req.getPathInfo().replaceFirst("/", "").substring(0, req.getPathInfo().replaceFirst("/", "").indexOf("/"))
		info("repositoryName: " + basePath)
		val gitdir = new File(basePath, repositoryName);
		val db = RepositoryCache.open(FileKey.lenient(gitdir, FS.DETECTED), true)

		val accessOk = GitStoreAuthHelper.checkAuth(req, db, writeAccess = false)
		if(!accessOk) {
			resp.setHeader("WWW-Authenticate", CHALLENGE)
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
		} else{

			super.service(req, resp)
		}
	}

}
