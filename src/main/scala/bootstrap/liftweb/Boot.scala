package bootstrap.liftweb

import code.helpers.DatabaseHelper
import code.model.Group
import code.model.Repository
import code.model.Repository_Group
import code.model.ServerSetup
import code.model.User
import code.model.UserGroups
import net.liftweb.common.Full
import net.liftweb.db.DB1.db1ToDb
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.auth.AuthRole
import net.liftweb.http.auth.HttpBasicAuthentication
import net.liftweb.http.auth.userRoles
import net.liftweb.http.LiftRules
import net.liftweb.http.NoticeType
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.mapper.By
import net.liftweb.mapper.DB
import net.liftweb.mapper.DefaultConnectionIdentifier
import net.liftweb.mapper.Schemifier
import net.liftweb.mapper.StandardDBVendor
import net.liftweb.sitemap.Loc.LinkText.strToLinkText
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Loc.Hidden
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import net.liftweb.util.Vendor.valToVender
import net.liftweb.util.Props
import net.liftweb.widgets.autocomplete.AutoComplete
import net.liftweb.widgets.flot._
import net.liftweb.widgets.sparklines.Sparklines
import net.liftweb.http.ParsePath
import code.model.Repository_User

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
	def boot {
		try {
			LiftRules.addToPackages("code")
			LiftRules.passNotFoundToChain
			// allow requests for git to pass straight through the LiftFilter 
			LiftRules.liftRequest.append({
				case r if (r.path.partPath match {
					case "git" :: _ => true
					case _ => false
				}) => false
			})

			AutoComplete.init
			Flot.init
			Sparklines.init

			if (!DB.jndiJdbcConnAvailable_?) {
				val vendor =
					new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
						Props.get("db.url") openOr
							"jdbc:h2:~/GitStore.db;AUTO_SERVER=TRUE",
						Props.get("db.user"), Props.get("db.password"))

				LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

				DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
			}
			Schemifier.schemify(true, Schemifier.infoF _, User, Group, Repository, UserGroups, Repository_Group, Repository_User, ServerSetup)
			//val loggedIn = If(() => !loggedIn_?, () => RedirectResponse("/tmc/tmc"))

			val serverSetup = ServerSetup.findAll
			if (serverSetup.isEmpty) {
				ServerSetup.create.name("Default GitStore server").basepath("/tmp/repos").save
			}

			def siteMap() = SiteMap(Menu(S ? "Commits") / "showcommits" >> Hidden,
				Menu(S ? "Show revision") / "showrev" >> Hidden,
				Menu(S ? "Show Repositories") / "index",
				Menu(S ? "Show Repository") / "showrepository" >> Hidden,
				Menu(S ? "Repository settings") / "repositorysettings" >> Hidden,
				Menu(S ? "Server admin") / "admin/serveradmin")

			LiftRules.setSiteMap(siteMap())

			//Show the spinny image when an Ajax call starts
			LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

			// Make the spinny image go away when it ends
			LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

			// Force the request to be UTF-8
			LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

			// What is the function to test if a user is logged in?
			/*LiftRules.loggedInTest = Full(() => {
				WebSession.loggedInUser.get match  {
					case Some(x) => true
					case None => false
				}
			})*/

			// Use HTML5 for rendering
			//LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

			LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full((1 seconds, 2 seconds)))
			// Make a transaction span the whole HTTP request
			//      S.addAround(DB.buildLoanWrapper

			DatabaseHelper.init

		} catch {
			case e: Throwable => e.printStackTrace
		}
	}
}


