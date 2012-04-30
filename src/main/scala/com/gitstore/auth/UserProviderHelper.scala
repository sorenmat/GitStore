package com.gitstore.auth
import code.model.ServerSetup
import ldap.LDAPGroupProvider
import com.gitstore.admin.UserProvider
import com.gitstore.auth.ldap.LDAPUserProvider
import com.gitstore.admin.DefaultUserProvider

object UserProviderHelper {

	def getUserProvider = {
		if (ServerSetup.instance.ldap_enabled.get) {
			println("Using LDAP user provider")
			new LDAPUserProvider
		} else {
			new DefaultUserProvider 
		}
	}
}