package dev.tvnclr.otpilot

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Every translatable string OTPilot adds (app/res/values/otpilot.xml and the tvnclr-ui library
 * strings) must exist in every locale the app ships (each values-* folder of the app that has a
 * strings.xml), with the same format arguments.
 * Prevents half-translated screens when a new string is added only in English.
 */
class OtpilotTranslationsTest {

  /** Every locale the app ships: the values-* folders of the app module that hold a strings.xml. */
  private val REQUIRED_LOCALES: List<String> by lazy {
    resDir("app")
        .listFiles { f -> f.isDirectory && f.name.startsWith("values-") && File(f, "strings.xml").exists() }!!
        .map { it.name.removePrefix("values-") }
        .sorted()
  }

  /** (base file, folder holding the values-* dirs) */
  private val sources =
      listOf(
          "otpilot.xml" to resDir("app"),
          "tvnclr_ui_strings.xml" to resDir("tvnclr-ui"),
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
    assertTrue("no locales found", REQUIRED_LOCALES.size > 1)
    assertTrue(problems.joinToString("\n", prefix = "Untranslated strings:\n"), problems.isEmpty())
  }
}
