package com.gitstore.auth.ldap
import com.gitstore.admin.GroupProvider
import com.gitstore.auth.LDAPUtil

class LDAPGroupProvider extends GroupProvider {
	override def groups: List[String] = LDAPUtil.getAllGroups
}