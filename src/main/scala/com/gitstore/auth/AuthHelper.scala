package com.gitstore.auth
import com.mongodb.BasicDBObject

import code.model.Repository

object AuthHelper {
	
	// TODO Refactor into "one" method
	def hasUserAccessRead(username: String, repoName: String) : Boolean = {
		val userGroups = LDAPUtil.getGroups(username)
		val repo = Repository.find(new BasicDBObject("name", repoName))
		val repoGroups = repo.get.read_groups.get
		repoGroups.find(g => userGroups.contains(g)) match {
			case Some(x) => return true
			case None => 
		}
		val repoWriteGroups = repo.get.read_write_groups.get
		repoWriteGroups.find(g => userGroups.contains(g)) match {
			case Some(x) => return true
			case _ => return false
		}
	}
	
	def hasUserAccessReadWrite(username: String, repoName: String) : Boolean = {
		val userGroups = LDAPUtil.getGroups(username)
		val repo = Repository.find(new BasicDBObject("name", repoName))
		val repoGroups = repo.get.read_write_groups.get
		repoGroups.find(g => userGroups.contains(g)) match {
			case Some(x) => return true
			case _ => return false
		}
	}
}