package com.gitstore

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.resolver.ReceivePackFactory
import org.eclipse.jgit.transport.ReceivePack
import javax.servlet.http.HttpServletRequest
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException
import org.eclipse.jgit.lib.Config.SectionParser
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.PersonIdent
import code.helpers.WebSession

class GitStoreReceivePackFactory extends ReceivePackFactory[HttpServletRequest] {

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
		//FIXME Check for security
		println("git store create...")

		val cfg = db.getConfig().get(CONFIG);
		var user = req.getRemoteUser();

		if (cfg.set) {
			if (cfg.enabled) {
				if (user == null || "".equals(user))
					user = "anonymous";
				return createFor(req, db, user);
			}
			throw new ServiceNotEnabledException();
		}

		if (user != null && !"".equals(user))
			return createFor(req, db, user);
		throw new ServiceNotAuthorizedException();
	}

	def createFor(req: HttpServletRequest, db: Repository, user: String): ReceivePack = {
		//FIXME Check for security
		val rp = new ReceivePack(db);
		rp.setRefLogIdent(toPersonIdent(req, user));
		return rp;
	}

	def toPersonIdent(req: HttpServletRequest, user: String): PersonIdent = {
		return new PersonIdent(user, user + "@" + req.getRemoteHost());
	}
}