import com.petebevin.markdown.MarkdownProcessor
object MarkdownTest {

	def main(args: Array[String]) {
		val m = new MarkdownProcessor()
		val html = m.markdown("This is a *simple* test.")
		println(html)
	}
}