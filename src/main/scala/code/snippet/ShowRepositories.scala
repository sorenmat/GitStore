package code.snippet

import java.io.File
import scala.collection.JavaConversions._
import scala.xml._
import scala.xml.NodeSeq
import scala.xml.Text
import org.eclipse.jgit.api._
import org.eclipse.jgit.storage.file._
import code.model._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb._
import net.liftweb.common.Full
import net.liftweb.common.Empty
import code.helpers.WebSession
import net.liftweb.widgets.flot.Flot
import net.liftweb.widgets.flot.FlotSerie
import net.liftweb.widgets.flot.FlotOptions
import net.liftweb.common.Failure
import net.liftweb.widgets.sparklines.Sparklines
import net.liftweb.http.js.JE.JsArray
import net.liftweb.widgets.sparklines.SparklineStyle
import net.liftweb.http.js.JE.JsObj
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import net.liftweb.common.Logger
import com.mongodb.BasicDBObject
import com.gitstore.auth.AuthHelper
import org.gitective.core.CommitFinder
import org.gitective.core.stat.AuthorHistogramFilter
import org.gitective.core.stat.CommitCalendar
import org.gitective.core.stat.Month

class ShowRepositories extends Logger {

	def getRepositoriesFromFileSystem = {
		val ss = ServerSetup.findAll.head
		val dir = new File(ss.basepath.get)
		if (dir.exists())
			dir.listFiles()
		else
			Array[File]()
	}

	def render(template: NodeSeq): NodeSeq = {
		val ss = ServerSetup.findAll.head
		val files = getRepositoriesFromFileSystem
		val repos = (files.filter(f => f.isDirectory())).filter(f => (new File(f, ".git").exists()))

		//		val userGroups = WebSession.loggedInUser.openOr(User).groups
		//		Repository.findAll.foreach(r => println("Found repository: "+r.name))
		val accessibleRepos = repos.filter(r => {
			if (!WebSession.loggedInUser.isEmpty) {

				val repo = Repository.find(new BasicDBObject("name", r.getName))
				println("Found repository in database: " + repo)
				val showRepo = repo match {
					case Full(x) => {
						AuthHelper.hasUserAccess(WebSession.loggedInUser.openOr(User).username.get, x.name.get)
					}
					case Empty => true
					case Failure(_, _, _) => false
				}
				println("showRepo: " + showRepo)
				println("isPublic: " + ss.public.get)
				if (showRepo || WebSession.loggedInUser.openOr(User).isAdmin.get)
					true
				else
					ss.public.get
			} else
				ss.public.get
		})
		accessibleRepos.flatMap(c => {
			val start = System.currentTimeMillis()
			val dataSet = {
				val opts = JsObj("background" -> "rgba(255,255,255,1)",
					"barColor" -> "red",
					"bar_color" -> "red")
				//					("fill_between_percentage_lines" -> true),
				//					("extend_markings" -> false))
				//				this.background = "rgba(255,255,255,1);";
				//  this.stroke = "rga(100,100,100,.7);";
				var repo: FileRepository = null
				try {
					try {
						val builder = new FileRepositoryBuilder();
						val gitFile = new File(c, ".git")
						repo = builder.setGitDir(gitFile)
							.readEnvironment() // scan environment GIT_* variables
							.findGitDir().build();

					} catch {
						case e: Throwable => e.printStackTrace()
					}
					val git = new Git(repo)

					val days = (0 until 30).toList.map(i => new LocalDate().minusDays(i) -> 0)
					val commitsPrDay = git.log.call().groupBy(r => new LocalDate(r.getCommitterIdent().getWhen()))
					val combinedMap = (days ++ commitsPrDay.map(e => e._1 -> e._2.size))

					val daysMap = combinedMap.sortWith((elm, elm1) => elm._1.isBefore(elm1._1)).map(f => f._2).toList
					repo.close()

					//					println(commitCountsPerDay.mkString("\n"))
					//					println("********")
					//					println(commitsPrDay.mkString("\n"))
					val data1 = JsArray(daysMap.map(f => JsArray(f)).takeRight(30): _*)

					(data1, opts)
				} catch {
					case e: Throwable => {
						e.printStackTrace()
						(JsArray(), opts)
					}
				}
			}
			val stop = System.currentTimeMillis()
			println("Generating stats for " + c.getName + " took " + (stop - start) + " ms.")
			var xml = bind("log", template,
				"reponame" -> link("/showrepository?repo=" + c.getName(), () => {}, Text(c.getName)),
				"graph" -%> <canvas id={ c.getName } style="width:140px;height:40px;"></canvas>)
			xml = xml ++ Sparklines.onLoad(c.getName, SparklineStyle.BAR, dataSet._1, dataSet._2)

			xml
		}).toSeq
	}
}
