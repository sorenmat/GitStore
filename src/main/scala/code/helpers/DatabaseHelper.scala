package code.helpers
import com.mongodb.DBObject
import code.model.User
import com.mongodb.BasicDBObject

object DatabaseHelper {
	def init {
		User.find(new BasicDBObject("username", "admin")).getOrElse({
			val user = User.createRecord.username("admin").password("admin").isAdmin(true)
			user.save
			user
		})

	}
}