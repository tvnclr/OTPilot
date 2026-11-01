package tvnclr.flightdeck.otpilot

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Every translatable string OTPilot adds (app/res/values/otpilot.xml and the flightdeck-ui library
 * strings) must exist in each locale listed in [REQUIRED_LOCALES], with the same format arguments.
 * Prevents half-translated screens when a new string is added only in English.
 */
class OtpilotTranslationsTest {

  private val REQUIRED_LOCALES = listOf("b+pt+BR")

  /** (base file, folder holding the values-* dirs) */
  private val sources =
      listOf(
          "otpilot.xml" to resDir("app"),
          "flightdeck_strings.xml" to resDir("flightdeck-ui"),
      )

  private fun resDir(module: String): File {
    // unit tests run with the module dir as working dir; resolve from the project root either way
    val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
    return File(root, "$module/src/main/res")
  }

  private fun strings(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
    val nodes = doc.getElementsByTagName("string")
    return (0 until nodes.length)
        .map { nodes.item(it) as Element }
        .filter { it.getAttribute("translatable") != "false" }
        .associate { it.getAttribute("name") to it.textContent }
  }

  private val formatArg = Regex("%(\\d+\\$)?[sdf]")

  @Test
  fun everyOtpilotStringIsTranslated() {
    val problems = mutableListOf<String>()
    for ((fileName, res) in sources) {
      val base = strings(File(res, "values/$fileName"))
      for (locale in REQUIRED_LOCALES) {
        val translated = strings(File(res, "values-$locale/$fileName"))
        for ((name, text) in base) {
          val t = translated[name]
          if (t == null) {
            problems += "$locale/$fileName: missing \"$name\""
          } else if (formatArg.findAll(text).map { it.value }.sorted().toList() !=
              formatArg.findAll(t).map { it.value }.sorted().toList()) {
            problems += "$locale/$fileName: format arguments differ in \"$name\""
          }
        }
      }
    }
    assertTrue(problems.joinToString("\n", prefix = "Untranslated strings:\n"), problems.isEmpty())
  }
}
