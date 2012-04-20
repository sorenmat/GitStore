package com.gitstore.admin
import code.model.User

trait UserProvider {
	def users: List[String]
}