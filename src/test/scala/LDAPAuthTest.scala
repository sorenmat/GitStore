import com.sun.jndi.ldap.LdapCtxFactory
import java.util.ArrayList
import java.util.Hashtable
import java.util.List
import java.util.Iterator
import javax.naming.Context
import javax.naming.AuthenticationException
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.directory.DirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.directory.SearchControls._
import code.helpers.LDAPHelper
import com.gitstore.auth.LDAPUtil
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.DefaultMongoIdentifier
import com.mongodb.Mongo

object LDAPAuthTest {

	def main(args: Array[String]) {
		MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "GitStore")
		println(LDAPUtil.getGroups("soren"))

	}
	
	def toDC(domainName: String ) =  {
        val buf = new StringBuilder();
        for (token <- domainName.split("\\.")) {
            if(token.length() !=0) {
            	if(buf.length()>0)  buf.append(",");
				buf.append("DC=").append(token);
            	
            }
        }
        buf.toString();
    }
}