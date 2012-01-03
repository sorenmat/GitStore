package code.helpers.git
import org.eclipse.jgit.diff.RawText
import java.io.ByteArrayOutputStream
import org.apache.commons.lang.StringUtils
import java.io.OutputStream

class GitStoreWeb(os: OutputStream) extends GitWebDiffFormatter(os) {

	var left, right = 0

	override def writeHunkHeader(aStartLine: Int, aEndLine: Int, bStartLine: Int, bEndLine: Int) {
		os.write("<tr><th>..</th><th>..</th><td class='hunk_header'>".getBytes());
		os.write('@');
		os.write('@');
		writeRange('-', aStartLine + 1, aEndLine - aStartLine);
		writeRange('+', bStartLine + 1, bEndLine - bStartLine);
		os.write(' ');
		os.write('@');
		os.write('@');
		os.write("</td></tr>\n".getBytes());
		left = aStartLine + 1;
		right = bStartLine + 1;
	}

	override def writeLine(prefix: Char, text: RawText, cur: Int) {
		os.write("<tr>".getBytes());
		prefix match {
			case '+' =>
				right = right + 1
				os.write(("<th></th><th>" + (right) + "</th>").getBytes());
				os.write("<td><div class=\"diff add2\">".getBytes());
			case '-' =>
				left = left + 1
				os.write(("<th>" + (left) + "</th><th></th>").getBytes());
				os.write("<td><div class=\"diff remove2\">".getBytes());
			case _ =>
				left = left + 1
				right = right + 1
				os.write(("<th>" + (left) + "</th><th>" + (right) + "</th>").getBytes());
				os.write("<td>".getBytes());
		}
		os.write(prefix);
		val bos = new ByteArrayOutputStream();
		text.writeLine(bos, cur);
		var line = bos.toString();
		line = escape(line, false);
		os.write(line.getBytes());
		prefix match {
			case '+' =>
				os.write("</div></td>".getBytes())
			case '-' =>
				os.write("</div></td>".getBytes());
			case _ =>
				os.write("</td>".getBytes());
		}
		os.write("</tr>\n".getBytes());
	}

	override def getHtml(): String = {
		val html = os.toString();
		var lines = html.split("\n");
		val sb = new StringBuilder();
		var inFile = false;
		val oldnull = "a/dev/null";
		for (curline <- lines) {
			var line = curline
			if (line.startsWith("index")) {
				// skip index lines
			} else if (line.startsWith("new file")) {
				// skip new file lines
			} else if (line.startsWith("\\ No newline")) {
				// skip no new line
			} else if (line.startsWith("---") || line.startsWith("+++")) {
				// skip --- +++ lines
			} else if (line.startsWith("diff")) {
				if (line.indexOf(oldnull) > -1) {
					// a is null, use b
					line = line.substring(("diff --git " + oldnull).length()).trim();
					// trim b/
					line = line.substring(2);
				} else {
					// use a
					line = line.substring("diff --git a/".length()).trim();
					line = line.substring(0, line.indexOf(" b/")).trim();
				}
				if (inFile) {
					sb.append("</tbody></table></div>\n");
					inFile = false;
				}
				sb.append("<div class='header'>").append(line).append("</div>");
				sb.append("<div class=\"diff\">");
				sb.append("<table><tbody>");
				inFile = true;
			} else {
				sb.append(line);
			}
		}
		if (inFile) {
			sb.append("</tbody>\n");
		}
		sb.append("</table></div>");
		return sb.toString();
	}

}