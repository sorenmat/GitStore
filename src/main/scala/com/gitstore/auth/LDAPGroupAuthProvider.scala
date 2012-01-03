package com.gitstore.auth

class LDAPGroupAuthProvider extends GroupAuthProvider {

	override def groups: List[String] = LDAPUtil.getGroups
}