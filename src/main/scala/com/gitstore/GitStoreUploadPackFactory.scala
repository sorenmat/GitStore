
package com.gitstore;

import org.eclipse.jgit.lib.Config.SectionParser
import scala.collection.JavaConversions._
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException
import org.eclipse.jgit.transport.resolver.UploadPackFactory
import org.eclipse.jgit.transport.UploadPack
import com.mongodb.BasicDBObject
import code.model.{ Repository => ModelRepo }
import javax.servlet.http.HttpServletRequest
import net.liftweb.mapper.By
import code.model.ServerSetup
import java.io.File
import scala.tools.nsc.dependencies.Files

/**
 * Create and configure {@link UploadPack} service instance.
 * <p>
 * Reading by upload-pack is permitted unless {@code http.uploadpack} is
 * explicitly set to false.
 */
class GitStoreUploadPackFactory extends UploadPackFactory[HttpServletRequest] {
	val CONFIG = new SectionParser[ServiceConfig]() {
		def parse(cfg: Config): ServiceConfig = {
			return new ServiceConfig(cfg);
		}
	};

	class ServiceConfig(cfg: Config) {
		val enabled = cfg.getBoolean("http", "uploadpack", true);
	}

	def create(req: HttpServletRequest, db: Repository): UploadPack = {
		//FIXME Check for security
//		GitStoreAuthHelper.checkAuth(req, db)
		if (db.getConfig().get(CONFIG).enabled)
			return new UploadPack(db)
		else
			throw new ServiceNotEnabledException()
	}

}
