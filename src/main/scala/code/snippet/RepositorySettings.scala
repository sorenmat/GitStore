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
class RepositorySettings {

	def render(form: NodeSeq) = {
		var userSelectedGroups: List[String] = Nil
		val repoName = WebSession.repository.get
		if (repoName == "")
			S.redirectTo("/")

		val repo = Repository.find(new BasicDBObject("name", repoName)).getOrElse({
			val newRepo = Repository.createRecord.name(repoName)
			newRepo
		})

		val groups = LDAPUtil.getGroups

		def checkAndSave(): Unit = {
			repo.groups(userSelectedGroups)
			println("saving groups: " + repo.groups)
			repo.save
		}

		def doBind(form: NodeSeq) = {
			val selectedGroups = repo.groups.get
			println("DB groups: " + selectedGroups.mkString(", "))
			val repoUsers = Nil //repo.users
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