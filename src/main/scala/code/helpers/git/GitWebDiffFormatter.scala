package code.helpers.git
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.DiffFormatter
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import org.apache.commons.lang.StringUtils
import org.eclipse.jgit.lib.Constants._

class GitWebDiffFormatter(os: OutputStream) extends DiffFormatter(os) {

	override def writeHunkHeader(aStartLine: Int, aEndLine: Int, bStartLine: Int, bEndLine: Int) {
		os.write("<div class=\"diff hunk_header\"><span class=\"diff hunk_info\">".getBytes())
		os.write("@@".getBytes())
		writeRange('-', aStartLine + 1, aEndLine - aStartLine)
		writeRange('+', bStartLine + 1, bEndLine - bStartLine)
		os.write(" @@".getBytes())
		os.write("</span></div>".getBytes())
	}

	def writeRange(prefix: Char, begin: Int, cnt: Int) {
		os.write(' ')
		os.write(prefix)
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
				os.write(encodeASCII(begin - 1))
				os.write(',')
				os.write('0')

			case 1 =>
				// If the range is exactly one line, produce only
				// the number.
				//
				os.write(encodeASCII(begin))

			case _ =>
				os.write(encodeASCII(begin))
				os.write(',')
				os.write(encodeASCII(cnt))
		}
	}

	override def writeLine(prefix: Char, text: RawText, cur: Int) {
		val bos = new ByteArrayOutputStream()
		text.writeLine(bos, cur)
		var line = bos.toString()
		line = escape(line, false)
		line = line.replaceAll("\n", "<br/>\n")

		val data = prefix match {
			case '+' =>
				"<span class=\"clean-gray\">"+line+"</span>\n"
			case '-' =>
				"<span class=\"diff-red\">"+line+"</span>\n"
			case _ => "<br/><br/><br/>\n"
		}
		os.write(data.getBytes)
	}

	def getHtml(): String = {
		val html = os.toString()
		val lines = html.split("\n")
		
		"<div class=\"diff\">"+lines.map(line => {
			if (line.startsWith("diff")) {
				"<div class=\"diff header\">"+line+"</div>"
			} else if (line.startsWith("---")) {
				"<span class=\"diff remove\">"+line+"</span><br/>"
			} else if (line.startsWith("+++")) {
				"<span class=\"diff add\">"+line+"</span><br/>"
			} else {
				line+'\n'
			}
		})+"</div>\n"
	}

	def escape(inStr: String, changeSpace: Boolean): String = {
		val s = inStr.map(c => c match {
			case '&' => "&amp;"
			case '<' => "&lt;"
			case '>' => "&gt;"
			case '\"' => "&quot;"
			case ' ' if (changeSpace) => "&nbsp;"
			case '\t' if (changeSpace) => "&nbsp;&nbsp;"
			case _ => c
		})
		s.mkString
	}

}