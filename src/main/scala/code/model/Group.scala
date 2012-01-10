//package code.model
//import net.liftweb.mapper._
//
//class Group extends LongKeyedMapper[Group] with IdPK with ManyToMany {
//	def getSingleton = Group
//
//	object groupname extends MappedString(this, 50)
//
//	object users extends MappedManyToMany(UserGroups, UserGroups.group, UserGroups.user, User)
//}
//
//object Group extends Group with LongKeyedMetaMapper[Group] {
//}
//
//// Many to Many
//object UserGroups extends UserGroups with LongKeyedMetaMapper[UserGroups]
//
//class UserGroups extends LongKeyedMapper[UserGroups] with IdPK {
//	def getSingleton = UserGroups
//	object user extends LongMappedMapper(this, User)
//	object group extends LongMappedMapper(this, Group)
//}
//
//// Many to Many
//object Repository_Group extends Repository_Group with LongKeyedMetaMapper[Repository_Group]
//
//class Repository_Group extends LongKeyedMapper[Repository_Group] with IdPK {
//	def getSingleton = Repository_Group
//	object repository extends LongMappedMapper(this, Repository)
//	object group extends LongMappedMapper(this, Group)
//}
//
//object Repository_User extends Repository_User with LongKeyedMetaMapper[Repository_User]
//
//class Repository_User extends LongKeyedMapper[Repository_User] with IdPK {
//	def getSingleton = Repository_User
//	object repository extends LongMappedMapper(this, Repository)
//	object user extends LongMappedMapper(this, User)
//}
