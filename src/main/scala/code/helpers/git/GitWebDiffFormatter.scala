package code.helpers.git
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.DiffFormatter
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import org.apache.commons.lang.StringUtils
import org.eclipse.jgit.lib.Constants._

class GitWebDiffFormatter(os: OutputStream) extends DiffFormatter(os) {

	/**
	 * Output a hunk header
	 *
	 * @param aStartLine
	 *            within first source
	 * @param aEndLine
	 *            within first source
	 * @param bStartLine
	 *            within second source
	 * @param bEndLine
	 *            within second source
	 * @throws IOException
	 */
	override def writeHunkHeader(aStartLine: Int, aEndLine: Int, bStartLine: Int, bEndLine: Int) {
		os.write("<div class=\"diff hunk_header\"><span class=\"diff hunk_info\">".getBytes());
		os.write('@');
		os.write('@');
		writeRange('-', aStartLine + 1, aEndLine - aStartLine);
		writeRange('+', bStartLine + 1, bEndLine - bStartLine);
		os.write(' ');
		os.write('@');
		os.write('@');
		os.write("</span></div>".getBytes());
	}

	def writeRange(prefix: Char, begin: Int, cnt: Int) {
		os.write(' ');
		os.write(prefix);
		cnt match {
			case 0 =>
				// If the range is empty, its beginning number must
				// be the
				// line just before the range, or 0 if the range is
				// at the
				// start of the file stream. Here, begin is always 1
				// based,
				// so an empty file would produce "0,0".
				//
				os.write(encodeASCII(begin - 1));
				os.write(',');
				os.write('0');

			case 1 =>
				// If the range is exactly one line, produce only
				// the number.
				//
				os.write(encodeASCII(begin));

			case _ =>
				os.write(encodeASCII(begin));
				os.write(',');
				os.write(encodeASCII(cnt));
		}
	}

	override def writeLine(prefix: Char, text: RawText, cur: Int) {
		val bos = new ByteArrayOutputStream();
		val myline = text.getString(1)
		text.writeLine(bos, cur);
		var line = bos.toString();
		line = escapeForHtml(line, false);
		line = line.replaceAll("\n", "<br/>\n")

		prefix match {
			case '+' =>
				os.write("<span class=\"clean-gray\">".getBytes());
				os.write(line.getBytes());
				os.write("</span>\n".getBytes());
			case '-' =>
				os.write("<span class=\"diff-red\">".getBytes());
				os.write(line.getBytes());
				os.write("</span>\n".getBytes());
			case _ => //os.write("<br/>\n".getBytes());
		}

	}

	/**
	 * Workaround function for complex private methods in DiffFormatter. This
	 * sets the html for the diff headers.
	 *
	 * @return
	 */
	def getHtml(): String = {
		val html = os.toString();
		val lines = html.split("\n");
		val sb = new StringBuilder();
		sb.append("<div class=\"diff\">");
		for (line <- lines) {
			if (line.startsWith("diff")) {
				sb.append("<div class=\"diff header\">").append(line).append("</div>");
			} else if (line.startsWith("---")) {
				sb.append("<span class=\"diff remove\">").append(line).append("</span><br/>");
			} else if (line.startsWith("+++")) {
				sb.append("<span class=\"diff add\">").append(line).append("</span><br/>");
			} else {
				sb.append(line).append('\n');
			}
		}
		sb.append("</div>\n");
		return sb.toString();
	}

	def escapeForHtml(inStr: String, changeSpace: Boolean): String = {
		val retStr = new StringBuffer();
		var i = 0;
		while (i < inStr.length()) {
			if (inStr.charAt(i) == '&') {
				retStr.append("&amp;");
			} else if (inStr.charAt(i) == '<') {
				retStr.append("&lt;");
			} else if (inStr.charAt(i) == '>') {
				retStr.append("&gt;");
			} else if (inStr.charAt(i) == '\"') {
				retStr.append("&quot;");
			} else if (changeSpace && inStr.charAt(i) == ' ') {
				retStr.append("&nbsp;");
			} else if (changeSpace && inStr.charAt(i) == '\t') {
				retStr.append(" &nbsp; &nbsp;");
			} else {
				retStr.append(inStr.charAt(i));
			}
			i = i + 1
		}
		return retStr.toString();
	}
}