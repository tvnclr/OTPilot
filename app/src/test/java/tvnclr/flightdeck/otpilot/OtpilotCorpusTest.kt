package tvnclr.flightdeck.otpilot

import io.github.jd1378.otphelper.utils.CodeExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

/**
 * OTPilot real-world regression corpus.
 * - otpilot_base1.yaml: validated cases, always asserted.
 * - otpilot_base2.yaml: new cases; asserted unless marked `pending: true`.
 */
@RunWith(Parameterized::class)
class OtpilotCorpusTest(
    private val name: String,
    private val message: String,
    private val expectedCode: String?,
    private val pending: Boolean,
) {
  companion object {
    private fun load(file: String, base: String): List<Array<Any?>> {
      val yaml = Yaml(SafeConstructor(LoaderOptions()))
      val stream = OtpilotCorpusTest::class.java.getResourceAsStream("/$file")!!
      val cases: List<Map<String, Any?>> = yaml.load(stream) ?: emptyList()
      return cases.map {
        arrayOf(
            "$base/${it["name"]}",
            it["message"] as String,
            it["expectedCode"]?.toString(),
            (it["pending"] as? Boolean) ?: false,
        )
      }
    }

    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun data(): List<Array<Any?>> =
        load("otpilot_base1.yaml", "base1") + load("otpilot_base2.yaml", "base2")
  }

  @Test
  fun extractsExpectedCode() {
    val extractor = CodeExtractor()
    assertFalse("[$name] should not be ignored", extractor.shouldIgnore(message))
    val code = extractor.getCode(message)
    if (pending) {
      println("[PENDING] $name expected=$expectedCode got=$code")
      return
    }
    assertEquals("[$name]", expectedCode, code)
  }
}
