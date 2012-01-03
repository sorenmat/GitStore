package code.helpers
import com.gitstore.auth.GroupHelper
import net.liftweb.common.Full
import code.model.Group
import net.liftweb.common.Empty
import code.model.Repository
import code.model.User
import net.liftweb.mapper.By
import net.liftweb.common.Failure
import code.model.ServerSetup

object DatabaseHelper {
	def init {

		GroupHelper.getGroupAuthProvider.groups.foreach(grp => {
			Group.find(By(Group.groupname, grp)) match {
				case Full(x) =>
				case Empty => {
					println("Creating group: " + grp)
					Group.create.groupname(grp).save
				}
				case Failure(_, _, _) =>
			}
		})

		User.find(By(User.username, "admin")) match {
			case Full(x) =>
			case Empty => {
				val admin = User.create.username("admin").password("admin").isAdmin(true)
				val adminGroup = Group.find(By(Group.groupname, "admin")).getOrElse({
					val grp = Group.create.groupname("admin")
					grp.save
					grp
				})
				admin.groups += adminGroup
				admin.save
			}
			case Failure(_, _, _) =>
		}

		
		LDAPHelper.sync
	}
}