package code.model
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedBoolean
import net.liftweb.record.field.StringField
import net.liftweb.record.field.BooleanField

class User private () extends MongoRecord[User] with ObjectIdPk[User] {
	def meta = User

	object username extends StringField(this, 40)
	object email extends StringField(this, 100)
	object password extends StringField(this, 40)
	object isAdmin extends BooleanField(this)
}

object User extends User with MongoMetaRecord[User]

//class User extends LongKeyedMapper[User] with IdPK with ManyToMany {
//	def getSingleton = User
//
//	object username extends MappedString(this, 40)
//	object email extends MappedString(this, 100)
//	object password extends MappedString(this, 40)
//	object isAdmin extends MappedBoolean(this)
//	object groups extends MappedManyToMany(UserGroups, UserGroups.user, UserGroups.group, Group)
//}
//
//object User extends User with LongKeyedMetaMapper[User] {}
//
