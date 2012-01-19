package com.gitstore.auth
import com.gitstore.admin.GroupProvider

class GroupAuthProvider extends GroupProvider {

	def groups: List[String] = List("admin", "users")
}