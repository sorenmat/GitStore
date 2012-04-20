package com.gitstore.auth.ldap
import com.gitstore.auth.LDAPUtil
import code.model.User
import com.mongodb.BasicDBObject
import net.liftweb.common.Full
import com.gitstore.admin.UserProvider

class LDAPUserProvider extends UserProvider {
	override def users: List[String] = LDAPUtil.getUsers
//	.map(user => {
//		val dbUser = User.find(new BasicDBObject("name", user))
//		dbUser match {
//			case Full(x) => x
//			case Empty => User.ce
//		}
//		dbUser
//	}})
}