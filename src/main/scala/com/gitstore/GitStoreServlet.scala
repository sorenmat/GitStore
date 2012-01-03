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

class GitStoreServlet extends GitServlet {
	val BASIC = "Basic"
	val CHALLENGE = "Basic" + " realm=\"Gitstore\""

	override def init(config: ServletConfig) {
		try {
			println("Starting git store servlet..")
			setReceivePackFactory(new GitStoreReceivePackFactory())
			setUploadPackFactory(new GitStoreUploadPackFactory())
			super.init(config)
		} catch {
			case t: Throwable => t.printStackTrace()
		}
	}

	override def service(req: HttpServletRequest, resp: HttpServletResponse) {
		val basePath = ServerSetup.findAll.head.basepath.toString
		val repositoryName = req.getPathInfo().replaceFirst("/", "").substring(0, req.getPathInfo().replaceFirst("/", "").indexOf("/"))
		val gitdir = new File(basePath, repositoryName);
		val db = RepositoryCache.open(FileKey.lenient(gitdir, FS.DETECTED), true)

		GitStoreAuthHelper.checkAuth(req, resp, db)
		super.service(req, resp)
	}

}
