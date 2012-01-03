package code.model
import net.liftweb.mapper._

class ServerSetup extends LongKeyedMapper[ServerSetup] with IdPK with ManyToMany {
	def getSingleton = ServerSetup

	object name extends MappedString(this, 100)
	object basepath extends MappedString(this, 100)

	// ldap
	object ldap_enabled extends MappedBoolean(this)
	object ldap_bind_url extends MappedString(this, 100)
	object ldap_bind_base extends MappedString(this, 100)
	object ldap_bind_dn extends MappedString(this, 100)
	object ldap_bind_pw extends MappedString(this, 100)
	
	object ldap_user_searchString extends MappedString(this, 200)

}

object ServerSetup extends ServerSetup with LongKeyedMetaMapper[ServerSetup] {

	def instance = {
		val server = ServerSetup.findAll.head
		server
	}

}

