package com.gitstore

import org.eclipse.jgit.lib.Repository
import scala.collection.JavaConversions._
import org.eclipse.jgit.transport.resolver.ReceivePackFactory
import org.eclipse.jgit.transport.ReceivePack
import javax.servlet.http.HttpServletRequest
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException
import org.eclipse.jgit.lib.Config.SectionParser
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.PersonIdent
import code.helpers.WebSession
import net.liftweb.common.Logger
import org.apache.commons.codec.binary.Base64

class GitStoreReceivePackFactory extends ReceivePackFactory[HttpServletRequest] with Logger {

	val CONFIG = new SectionParser[ServiceConfig]() {
		def parse(cfg: Config): ServiceConfig = {
			return new ServiceConfig(cfg);
		}
	};

	class ServiceConfig(cfg: Config) {
		println("service config")
		val set = cfg.getString("http", null, "receivepack") != null;
		val enabled = cfg.getBoolean("http", "receivepack", false);
	}

	override def create(req: HttpServletRequest, db: Repository): ReceivePack = {
		println("git store create...")
		try {

			val cfg = db.getConfig().get(CONFIG);
			GitStoreAuthHelper.getUsernameAndPasswordFromRequest(req) match {
				case Some(x) => {
					var user = x._1
					GitStoreAuthHelper.checkAuth(req, null, db, writeAccess = false) // resp null, should not fail with auth exception
					// TODO Check for push access..

					//			val usernameAndPassword = GitStoreAuthHelper.getUsernameAndPasswordFromRequest(req)
					//			println("USER INFO: "+usernameAndPassword._1+" - "+usernameAndPassword._2)

					//			info("User principal " + req.getUserPrincipal())
					//			if (cfg.set) {
					//				if (cfg.enabled) {
					//					info("Config enabled")
					//					if (user == null || "".equals(user))
					//						user = "anonymous";
					//					return createFor(req, db, user);
					//				}
					//				throw new ServiceNotEnabledException();
					//			}
					//			info("Remote User " + user)

					//			if (user != null && !"".equals(user))
					return createFor(req, db, user);
					//			throw new ServiceNotAuthorizedException();
				}
				case None => 
			}
		} catch {
			case t: Throwable => {
				t.printStackTrace()
				error(t)
			}
		}
		throw new RuntimeException("Should not occur...")
	}

	def createFor(req: HttpServletRequest, db: Repository, user: String): ReceivePack = {
		val rp = new ReceivePack(db);
		rp.setRefLogIdent(toPersonIdent(req, user));
		return rp;
	}

	def toPersonIdent(req: HttpServletRequest, user: String): PersonIdent = {
		return new PersonIdent(user, user + "@" + req.getRemoteHost());
	}
}