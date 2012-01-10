import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.scalatest.TestFailedException
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SampleTest extends Spec with ShouldMatchers {
  describe("simple test case") {
    it("should run") {
      1 + 1 should be (2)    
    }
    it("should multiply two number") {
      10 * 10 should be (100)    
    }
  }
}