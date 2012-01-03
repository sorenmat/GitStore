package code.helpers.git
import java.io.IOException
import java.io.OutputStream
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.Date
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.revwalk.RevCommit
import scala.collection.JavaConversions._
import org.apache.commons.lang.StringUtils

class PatchFormatter(os: OutputStream) extends DiffFormatter(os) {

	var changes = Map[String, PatchTouple]()
	var currentTouple: PatchTouple = _

	override def format(entry: DiffEntry) {
		currentTouple = new PatchTouple();
		changes += entry.getNewPath() -> currentTouple
		super.format(entry);
	}

	override def writeLine(prefix: Char, text: RawText, cur: Int) {
		prefix match {
			case '+' => currentTouple.insertions = currentTouple.insertions + 1
			case '-' => currentTouple.deletions = currentTouple.deletions + 1
			case x => println("default case: '"+x+"'")
		}
		super.writeLine(prefix, text, cur);
	}

	def getPatch(commit: RevCommit): String = {
		val patch = new StringBuilder();
		// hard-code the mon sep 17 2001 date string.
		// I have no idea why that is there. it seems to be a constant.
		patch.append("From " + commit.getName() + " Mon Sep 17 00:00:00 2001" + "\n");
		patch.append("From: " + commit.getAuthorIdent() + "\n");
		patch.append("Date: "
			+ (new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date(commit
				.getCommitTime() * 1000L))) + "\n");
		patch.append("Subject: [PATCH] " + commit.getShortMessage() + "\n");
		patch.append('\n');
		patch.append("---");
		var maxPathLen = 0;
		var files = 0;
		var insertions = 0;
		var deletions = 0;
		changes.foreach(entry => {
			val path = entry._1
			if (path.length() > maxPathLen) {
				maxPathLen = path.length();
			}
			val touple = entry._2
			files =files+1
			insertions = insertions+ touple.insertions
			deletions = deletions+touple.deletions
		})
		val columns = 60;
		val total = insertions + deletions;
		var unit = total / columns + (if (total % columns > 0) 1 else 0)
		if (unit == 0) {
			unit = 1;
		}
		changes.foreach(entry => {
			val path = entry._1
			val touple = entry._2
			patch.append("\n " + StringUtils.rightPad(path, maxPathLen, ' ') + " | "
				+ StringUtils.leftPad("" + touple.total, 4, ' ') + " "
				+ touple.relativeScale(unit));
		})
		patch.append("\n "+files+" files changed, "+insertions+" insertions(+), "+deletions+" deletions(-)\n\n")
		patch.append(os.toString());
		patch.append("\n--\n");
		patch.append("GitStore version 1.1");
		return patch.toString();
	}
}

/**
 * Class that represents the number of insertions and deletions from a
 * chunk.
 */
class PatchTouple {
	var insertions = 0
	var deletions = 0

	def total = insertions + deletions;

	def relativeScale(unit: Int): String = {
		val plus = insertions / unit;
		val minus = deletions / unit;
		val sb = new StringBuilder();
		for (i <- 0 until plus) {
			sb.append('+');
		}
		for (i <- 0 until minus) {
			sb.append('-');
		}
		return sb.toString();
	}
}