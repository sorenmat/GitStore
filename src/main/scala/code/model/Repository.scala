package code.model
import net.liftweb.mapper._
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.MongoJsonObjectListField
import net.liftweb.mongodb.record.field.MongoListField

class Repository private() extends MongoRecord[Repository] with ObjectIdPk[Repository] {
  def meta = Repository

  object name extends StringField(this, 100)
  object path extends StringField(this, 100)
  object groups extends MongoListField[Repository, String](this)
  
}

object Repository extends Repository with MongoMetaRecord[Repository]
