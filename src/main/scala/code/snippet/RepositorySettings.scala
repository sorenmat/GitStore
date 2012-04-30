package code.snippet
import scala.xml.NodeSeq
import com.gitstore.auth.GroupHelper
import com.gitstore.auth.LDAPUtil
import com.mongodb.BasicDBObject
import code.helpers.WebSession
import code.model.Repository
import net.liftweb.common.Box.box2Option
import net.liftweb.http.SHtml.multiSelect
import net.liftweb.http.SHtml.submit
import net.liftweb.http.S
import net.liftweb.util.Helpers.bind
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.widgets.autocomplete.AutoComplete
import com.gitstore.auth.UserProviderHelper

class RepositorySettings extends RepositoryContextPage {

	def content(form: NodeSeq) = {
		var userSelectedGroups: List[String] = Nil

		val repo = Repository.find(new BasicDBObject("name", repositoryname)).getOrElse({
			val newRepo = Repository.createRecord.name(repositoryname)
			newRepo
		})

		val groups = GroupHelper.getGroupAuthProvider.groups
		println("Found the following groups " + groups.mkString(","))

		def checkAndSave(): Unit = {
			repo.read_write_groups(userSelectedGroups)
			println("saving groups: " + repo.read_write_groups)
			repo.save
		}

		def doBind(form: NodeSeq) = {
			val selectedGroups = repo.read_write_groups.get
			println("DB groups: " + selectedGroups.mkString(", "))
			val repoUsers = UserProviderHelper.getUserProvider.users
			bind("serveradmin", form,
				"groups" -> multiSelect(GroupHelper.getGroupAuthProvider.groups.map(r => (r, r)).toSeq, selectedGroups, selected => userSelectedGroups = selected),
				"users" -> multiSelect(repoUsers.map(u => (u.toString, u.toString)), Seq(), v => {}), //FIXME maybe.
				"adduser" -> AutoComplete("", (current, limit) => {
					groups.filter(f => f.startsWith(current))
				},
					value => println("Submitted: " + value)),
				"submit" -> submit("Save", checkAndSave))
		}

		doBind(form)
	}

}