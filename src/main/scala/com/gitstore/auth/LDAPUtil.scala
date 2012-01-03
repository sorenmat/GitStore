package com.gitstore.auth
import java.util.Hashtable
import java.util.logging.Level
import java.util.logging.Logger
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.directory.BasicAttributes
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import code.model.ServerSetup
import collection.JavaConversions._

object LDAPUtil {

	val url = ServerSetup.instance.ldap_bind_url.toString()
	val base = ServerSetup.instance.ldap_bind_base.toString()

	val dn = ServerSetup.instance.ldap_bind_dn.toString()
	val ldap_bind_pw = ServerSetup.instance.ldap_bind_pw.toString()
	val ldap_user_searchString = ServerSetup.instance.ldap_user_searchString.toString()

	def search(username: String, password: String, projectName: List[String]) = {
		connecetAndBind(username, password, projectName)
	}

	def getGroups: List[String] = {
		val query = "(objectclass=group)"
		try {
			val env = new Hashtable[String, String]()

			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
			env.put(Context.PROVIDER_URL, url)

			env.put(Context.SECURITY_AUTHENTICATION, "simple")
			env.put(Context.SECURITY_PRINCIPAL, dn)
			env.put(Context.SECURITY_CREDENTIALS, ldap_bind_pw)
			val context = new InitialDirContext(env)

			val ctrl = new SearchControls()
			ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE)
			println("Connect and bind query: " + query)
			val enumeration = context.search("", query, ctrl)
			val groups = enumeration.map(e => {
				val attribs = e.asInstanceOf[SearchResult].getAttributes()
				attribs.get("cn").get().toString()

			})
			return groups.toList
		} catch {
			case e: Exception => e.printStackTrace()
		}
		return Nil
	}

	def getUsers: List[String] = {
		val query = ldap_user_searchString
		try {
			val env = new Hashtable[String, String]()

			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
			env.put(Context.PROVIDER_URL, url)

			env.put(Context.SECURITY_AUTHENTICATION, "simple")
			env.put(Context.SECURITY_PRINCIPAL, dn)
			env.put(Context.SECURITY_CREDENTIALS, ldap_bind_pw)
			val context = new InitialDirContext(env)

			val ctrl = new SearchControls()
			ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE)
			println("Connect and bind query: " + query)
			val enumeration = context.search("", query, ctrl)
			val groups = enumeration.map(e => {
				val attribs = e.asInstanceOf[SearchResult].getAttributes()
//				println("Username -> " + attribs)
				println("****************")
				attribs.get("samaccountname").get().toString()

			})
			return groups.toList
		} catch {
			case e: Exception => e.printStackTrace()
		}
		return Nil
	}

	/**
	 * Connect to the ldap server as the bind user, and search for the user in the attribute sAMAccountName
	 * If found call the searchUser method with the distinguishedName of the user
	 * @param username
	 * @param projectName
	 * @param password
	 * @return
	 */
	def connecetAndBind(username: String, password: String, projectName: List[String]): Boolean = {
		val query = "(sAMAccountName=" + username + ")"
		try {
			val env = new Hashtable[String, String]()

			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
			env.put(Context.PROVIDER_URL, url)

			env.put(Context.SECURITY_AUTHENTICATION, "simple")
			env.put(Context.SECURITY_PRINCIPAL, dn)
			env.put(Context.SECURITY_CREDENTIALS, ldap_bind_pw)
			val context = new InitialDirContext(env)

			val ctrl = new SearchControls()
			ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE)
			println("Connect and bind query: " + query)
			val enumeration = context.search("", query, ctrl)
			while (enumeration.hasMore()) {
				val result = enumeration.next().asInstanceOf[SearchResult]
				val attribs = result.getAttributes()
				val bas = attribs.asInstanceOf[BasicAttributes]
				val all = bas.getAll()
				val memberof = bas.get("distinguishedName")
				return searchUser(memberof.toString(), password, projectName)
			}
		} catch {
			case e: Exception => e.printStackTrace()
		}
		return false
	}

	/**
	 * Searches for distinguishedName found by the bind user, and checks if the user has the project name in the
	 * memberof attribute
	 *
	 * @param projectName
	 * @param dn
	 * @param password
	 * @return
	 */
	def searchUser(origdn: String, password: String, projectName: List[String]): Boolean = {
		try {
			val dn = origdn.replaceAll("distinguishedName: ", "")
			val query = "(distinguishedName=" + dn + ")"
			println("Query: " + query)
			val env = new Hashtable[String, String]()

			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
			env.put(Context.PROVIDER_URL, url)

			env.put(Context.SECURITY_AUTHENTICATION, "simple")
			env.put(Context.SECURITY_PRINCIPAL, dn)
			env.put(Context.SECURITY_CREDENTIALS, password)
			val context = new InitialDirContext(env)

			val ctrl = new SearchControls()
			ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE)

			val enumeration = context.search("", query, ctrl)
			while (enumeration.hasMore()) {
				val result = enumeration.next().asInstanceOf[SearchResult]
				val attribs = result.getAttributes()
				val bas = attribs.asInstanceOf[BasicAttributes]

				val memberof = bas.get("memberof")
				memberof.getAll().foreach(grp => println("grp: " + grp))

				for (project <- projectName) {
					println("project: " + project)
					if (memberof.toString().contains("CN=" + project)) {
						return true
					}
				}
			}

			val enumeration1 = context.search("", base, ctrl)
			while (enumeration1.hasMore()) {
				val result = enumeration1.next().asInstanceOf[SearchResult]
				val attribs = result.getAttributes()
				val bas = attribs.asInstanceOf[BasicAttributes]

				val memberof = bas.get("memberof")
				memberof.getAll().foreach(grp => println("Groups: " + grp))

				for (project <- projectName) {
					println("project ->: " + project)
					if (memberof.toString().contains("CN=" + project)) {
						return true
					}
				}
			}
		} catch {
			case e: Exception => e.printStackTrace()
		}
		return false
	}

}
