package code.snippet
import code.model.Repository
import code.model.ServerSetup
import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb._
import http._
import SHtml._
import S._
import js._
import JsCmds._
import mapper._
import util._
import Helpers._
import code.helpers.WebSession
import scala.xml.Text
import net.liftweb.http.jquery.JqSHtml
import net.liftweb.widgets.autocomplete._
import com.gitstore.auth.GroupHelper
import code.model.Group
import code.model.User
class RepositorySettings {

	def render(form: NodeSeq) = {
		var userSelectedGroups: List[String] = Nil
		val repoName = WebSession.repository.get
		val repo = Repository.find(By(Repository.name, repoName)).getOrElse({
			val newRepo = Repository.create.name(repoName)
			newRepo
			})

		def checkAndSave(): Unit = {
				val groups = Group.findAll(ByList(Group.groupname, userSelectedGroups))
				println("groups: "+groups)
				repo.groups.clear()
				repo.groups ++= groups
				repo.save
		}

		def doBind(form: NodeSeq) = {
			val selectedGroups = repo.groups.map(g => g.groupname.toString)
			println("DB groups: "+selectedGroups.mkString(", "))
			val repoUsers = repo.users
			bind("serveradmin", form,
				"groups" -> multiSelect(GroupHelper.getGroupAuthProvider.groups.map(r => (r, r)).toSeq, selectedGroups, selected => userSelectedGroups = selected),
				"users" -> multiSelect(repoUsers.map(u => (u.toString, u.toString)), Seq(), v => {}), //FIXME maybe.
				"adduser" -> AutoComplete("", (current, limit) => {
					//FIXME this might be slow...
					User.findAll(Like(User.username, current+"%")).map(user => user.username.toString)
				},
					value => println("Submitted: " + value)),
				"submit" -> submit("Save", checkAndSave))
		}
		
		
	
			

		doBind(form)
	}

}