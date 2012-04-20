package code.model
import net.liftweb.mapper._
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.StringField
import net.liftweb.record.field.PasswordField
import net.liftweb.record.field.BooleanField

class ServerSetup extends MongoRecord[ServerSetup] with ObjectIdPk[ServerSetup] {
	def meta = ServerSetup

	object name extends StringField(this, 100)
	object basepath extends StringField(this, 100)
	object public extends BooleanField(this)
	
	// ldap
	object ldap_enabled extends BooleanField(this)
	object hostname extends StringField(this, 100)
	object ldap_bind_base extends StringField(this, 100)
	object ldap_bind_dn extends StringField(this, 100)
	object ldap_bind_pw extends StringField(this, 100)

	object ldap_user_searchString extends StringField(this, 200)
	object ldap_group_searchString extends StringField(this, 200)

}

object ServerSetup extends ServerSetup with MongoMetaRecord[ServerSetup] {

	def instance = {
		val server = ServerSetup.findAll.head
		server
	}

}
