package com.gitstore.auth
import code.model.ServerSetup

object GroupHelper {

	def getGroupAuthProvider = {
		if (ServerSetup.instance.ldap_enabled.get) {
			new LDAPGroupAuthProvider
		} else {
			new GroupAuthProvider // TODO check if ldap is enabled and return that...
		}
	}
}