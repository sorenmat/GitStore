import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import java.io.File
import scala.collection.JavaConversions._

object GitLogTest {

	def main(args: Array[String]) {
		val builder = new FileRepositoryBuilder();
		val repository = builder.setGitDir(new File("/Users/soren/tmp/sbt-release/.git"))
			.readEnvironment() // scan environment GIT_* variables
			 .findGitDir().build();
		
		val git = new Git(repository)
//		for (c <- git.log().call())
//			println(c.getId() + "/" + c.getAuthorIdent().getName() + "/" + c.getShortMessage());
		val commitsPrDay = git.log.call().groupBy(r => new java.text.SimpleDateFormat("yyyy/MM/dd").format(r.getCommitterIdent().getWhen())).toList.sort( (a,b) =>
			a._1 > b._1)
		val commitCountsPerDay = commitsPrDay.map( f => (f._1, f._2.size))
		println(commitCountsPerDay.mkString("\n"))
		println("********")
		println(commitsPrDay.mkString("\n"))

	}
}