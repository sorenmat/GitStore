package com.gitstore.auth
import java.util.Hashtable

import scala.collection.JavaConversions.enumerationAsScalaIterator

import com.sun.jndi.ldap.LdapCtxFactory

import code.model.ServerSetup
import javax.naming.directory.SearchControls.SUBTREE_SCOPE
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.Context
import scala.collection.JavaConversions._

object LDAPUtil {

	val hostname = if ("" != ServerSetup.instance.hostname.get)
		ServerSetup.instance.hostname.get
	else
		"localhost.localdomain"
	val base = ServerSetup.instance.ldap_bind_base.toString()

	val dn = ServerSetup.instance.ldap_bind_dn.toString()
	val ldap_bind_pw = ServerSetup.instance.ldap_bind_pw.toString()
	val ldap_user_searchString = ServerSetup.instance.ldap_user_searchString.toString()

	def getGroups: List[String] = {
		val query = ServerSetup.instance.ldap_group_searchString.get
		try {
			val env = new Hashtable[String, String]()

			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
			println("base = " + base)
			env.put(Context.PROVIDER_URL, url + base)

			env.put(Context.SECURITY_AUTHENTICATION, "simple")
			env.put(Context.SECURITY_PRINCIPAL, dn)
			env.put(Context.SECURITY_CREDENTIALS, ldap_bind_pw)
			val context = new InitialDirContext(env)
			val ctrl = new SearchControls()
			ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE)
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
			val enumeration = context.search("", query, ctrl)
			val groups = enumeration.map(e => {
				val attribs = e.asInstanceOf[SearchResult].getAttributes()
				attribs.get("samaccountname").get().toString()

			})
			return groups.toList
		} catch {
			case e: Exception => e.printStackTrace()
		}
		return Nil
	}

	def getGroups(username: String): List[String] = {
		val domainName = hostname.substring(hostname.indexOf(".") + 1, hostname.length())
		val serverName = hostname.substring(0, hostname.indexOf("."))

		System.out.println("Authenticating " + username + "@" + domainName + " through " + serverName + "." + domainName);

		// bind by using the specified username/password
		val props = new Hashtable[String, String]()
		val principalName = username + "@" + domainName;
		props.put(Context.SECURITY_PRINCIPAL, dn)
		props.put(Context.SECURITY_CREDENTIALS, ldap_bind_pw);

		val context = LdapCtxFactory.getLdapCtxInstance(url, props);
		System.out.println("Authentication succeeded!");

		// locate this user's record
		val controls = new SearchControls();
		controls.setSearchScope(SUBTREE_SCOPE);
		val renum = context.search(toDC(domainName), "(& (userPrincipalName=" + principalName + "))", controls);
		if (!renum.hasMore()) {
			System.out.println("Cannot locate user information for " + username);
			Nil
		} else {
			val groups = renum.flatMap(f => {
				val memberof = f.getAttributes().get("memberof").getAll
				val groups = memberof.map(f => {

					val group = f.toString()
					val gruopName = group.substring(0, group.indexOf(",")).replace("CN=", "")
					gruopName

				})
				groups
			})
			return groups.toList
		}

	}

	def url = {
		println("hostname == " + hostname)
		val domainName = hostname.substring(hostname.indexOf(".") + 1, hostname.length())
		val serverName = hostname.substring(0, hostname.indexOf("."))
		"ldap://" + serverName + "." + domainName + '/'
	}

	def authenticate(username: String, password: String): Boolean = {
		val domainName = hostname.substring(hostname.indexOf(".") + 1, hostname.length())
		val serverName = hostname.substring(0, hostname.indexOf("."))

		System.out.println("Authenticating " + username + "@" + domainName + " through " + serverName + "." + domainName);

		// bind by using the specified username/password
		val props = new Hashtable[String, String]()
		val principalName = username + "@" + domainName;
		props.put(Context.SECURITY_PRINCIPAL, principalName);
		props.put(Context.SECURITY_CREDENTIALS, password);

		val context = LdapCtxFactory.getLdapCtxInstance(url, props);
		System.out.println("Authentication succeeded!");

		// locate this user's record
		val controls = new SearchControls();
		controls.setSearchScope(SUBTREE_SCOPE);
		val renum = context.search(toDC(domainName), "(& (userPrincipalName=" + principalName + ")(objectClass=user))", controls);
		if (!renum.hasMore()) {
			System.out.println("Cannot locate user information for " + username);
			false
		} else {
			true
		}

	}

	def toDC(domainName: String) = {
		val buf = new StringBuilder();
		for (token <- domainName.split("\\.")) {
			if (token.length() != 0) {
				if (buf.length() > 0) buf.append(",");
				buf.append("DC=").append(token);

			}
		}
		buf.toString();
	}
}
