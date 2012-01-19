package code.snippet.admin
import code.model.Repository
import code.model.ServerSetup
import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb._
import http._
import SHtml._
import S._

import js._
import JsCmds._

import mapper._

import util._
import Helpers._

class ServerAdmin {
	def save(form: NodeSeq) = {
		val servetsetup = ServerSetup.findAll.head

		def checkAndSave(): Unit =
			servetsetup.validate match {
				case Nil => servetsetup.save; S.notice("Added " + servetsetup.name)
				case xs => S.error(xs); S.mapSnippet("ServerAdmin.save", doBind)
			}

		def doBind(form: NodeSeq) =
			bind("serveradmin", form,
				"basepath" -> servetsetup.basepath.toForm,
				"repositoryname" -> servetsetup.name.toForm,
				"public" -> servetsetup.public.toForm,

				"ldap_enabled" -> servetsetup.ldap_enabled.toForm,
				"ldap_hostname" -> servetsetup.hostname.toForm,

				"ldap_base" -> servetsetup.ldap_bind_base.toForm,
				"ldap_dn" -> servetsetup.ldap_bind_dn.toForm,
				"ldap_pw" -> servetsetup.ldap_bind_pw.toForm,

				"ldap_user_search" -> servetsetup.ldap_user_searchString.toForm,
				"ldap_group_search" -> servetsetup.ldap_group_searchString.toForm,

				"submit" -> submit("Save", checkAndSave))

		doBind(form)
	}
}