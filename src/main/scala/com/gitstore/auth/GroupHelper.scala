package com.gitstore.auth
import code.model.ServerSetup
import ldap.LDAPGroupProvider

object GroupHelper {

	def getGroupAuthProvider = {
		if (ServerSetup.instance.ldap_enabled.get) {
			new LDAPGroupProvider
//			new LDAPGroupProvider
		} else {
			new GroupAuthProvider // TODO check if ldap is enabled and return that...
		}
	}
}