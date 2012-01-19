package com.gitstore.auth
import com.mongodb.BasicDBObject

import code.model.Repository

object AuthHelper {
	def hasUserAccess(username: String, repoName: String) : Boolean = {
		val userGroups = LDAPUtil.getGroups(username)
		val repo = Repository.find(new BasicDBObject("name", repoName))
		val repoGroups = repo.get.groups.get
		repoGroups.find(g => userGroups.contains(g)) match {
			case Some(x) => return true
			case _ => return false
		}
	}
}