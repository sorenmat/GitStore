package code.model
import net.liftweb.mapper._

class User extends LongKeyedMapper[User] with IdPK with ManyToMany {
	def getSingleton = User

	object username extends MappedString(this, 40)
	object email extends MappedString(this, 100)
	object password extends MappedString(this, 40)
	object isAdmin extends MappedBoolean(this)
	object groups extends MappedManyToMany(UserGroups, UserGroups.user, UserGroups.group, Group)
}

object User extends User with LongKeyedMetaMapper[User] {}

