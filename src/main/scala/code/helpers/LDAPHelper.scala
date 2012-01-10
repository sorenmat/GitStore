package code.helpers

import code.model.ServerSetup
import com.gitstore.auth.LDAPUtil
import code.model.User
import net.liftweb.mapper.By
import net.liftweb.common.Full
import net.liftweb.common.Empty
object LDAPHelper {
	def sync {
//		try {
//			val ldapGroups = LDAPUtil.getGroups
//			val serverGroups = Group.findAll()
//			serverGroups.foreach(g =>
//				if (!ldapGroups.contains(g.toString()))
//					g.delete_!)
//			ldapGroups.foreach(g =>
//				if (!serverGroups.map(e => e.toString).toList.contains(g))
//					Group.create.groupname(g).save)
//
//			val ldapUsers = LDAPUtil.getUsers
//			val dbUsers = User.findAll()
//			
//			dbUsers.foreach(u => ldapUsers.find(usr => usr  == u.username.toString()) match {
//				case Some(x) => println("User '"+x+"' was in both ldap and database")
//				case None => u.delete_!
//			})
//			ldapUsers.foreach(usr => User.find(By(User.username, usr)) match {
//				case Full(x) => 
//				case Empty => User.create.username(usr).save
//			})
//		} catch {
//			case e: Throwable => e.printStackTrace()
//		}
	}
}