package code.model
import net.liftweb.mapper._

class Repository extends LongKeyedMapper[Repository] with IdPK with ManyToMany {
	def getSingleton = Repository

	object name extends MappedString(this, 100)
	object path extends MappedString(this, 100)

	object groups extends MappedManyToMany(Repository_Group, Repository_Group.repository , Repository_Group.group, Group)
	object users extends MappedManyToMany(Repository_User, Repository_User.repository , Repository_User.user, User)
}

object Repository extends Repository with LongKeyedMetaMapper[Repository] {}

