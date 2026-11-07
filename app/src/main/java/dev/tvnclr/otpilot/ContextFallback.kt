package dev.tvnclr.otpilot

/**
 * OTPilot context fallback.
 *
 * Runs only when the upstream matchers found a code keyword ("code", "código", ...) but no code
 * next to it, e.g. "use the verification code below to proceed ... process. 316974 Thank you".
 * It picks the first standalone 4-8 digit number that appears shortly AFTER a keyword, skipping
 * numbers that look like dates, times, amounts, years, phone numbers or references.
 * Nothing upstream already detects is changed: this only runs where upstream returns null.
 *
 * Two limits keep it from grabbing unrelated numbers (e.g. "VS Code" in the body and the sender's
 * ZIP code in the footer):
 * - a bare "code"/"código" only counts when the words around it make it a sign-in style code
 *   ("verification code", "your code", "code below", "código de acesso", ...);
 * - the number must start within [MAX_DISTANCE] characters of the keyword.
 */
object ContextFallback {
  /** How far after the keyword the code may start, in characters. */
  const val MAX_DISTANCE = 200

  /** A keyword and the code found after it. */
  data class Hit(val keyword: IntRange, val code: IntRange, val value: String)

  // Arabic-Indic and extended Arabic-Indic digits are accepted like the upstream matcher.
  private const val D = "\\d\\u0660-\\u0669\\u06F0-\\u06F9"

  private val candidate =
      Regex(
          // not glued to another digit/letter, nor to date/amount/ref/phone punctuation
          "(?<![${D}\\p{L}#\$€£¥₹+/\\\\_.,:-])" +
              // not the second group of a space-separated number (phone numbers)
              "(?<![${D}] )" +
              "([${D}]{4,8})" +
              // not followed by more digits/letters, a unit/percent, or a date/time/decimal part
              "(?![${D}\\p{L}%/\\\\_-])(?![.,:][${D}])" +
              // not the first group of a space-separated number
              "(?! [${D}])")

  private val yearLike = Regex("^(19|20)\\d{2}$")

  /** Keywords too generic to trust alone: they need a qualifier next to them. */
  private val genericKeyword = Regex("^(?:code|c[oó]digo)$", RegexOption.IGNORE_CASE)

  /** Words right before a generic keyword that make it a sign-in style code. */
  private val qualifierBefore =
      Regex(
          "(?:verification|security|confirmation|login|log-?in|sign-?in|access|authentication|" +
              "auth|activation|one[-\\s]time|single[-\\s]use|otp|2fa|your|this|the following|" +
              "seu|este|esse|o seguinte)[\\s:]*$",
          RegexOption.IGNORE_CASE)

  /** Words right after a generic keyword that make it a sign-in style code. */
  private val qualifierAfter =
      Regex(
          "^[\\s:]*(?:below|is\\b|to (?:verify|confirm|sign|log|complete|reset|continue)|" +
              "abaixo|é\\b|para (?:verificar|confirmar|entrar|acessar|continuar|concluir)|" +
              "de (?:verifica|seguran|acesso|confirma|autentica|login|ativa|uso [úu]nico))",
          RegexOption.IGNORE_CASE)

  private fun qualifies(text: String, keyword: IntRange): Boolean {
    if (!genericKeyword.matches(text.substring(keyword))) return true
    val before = text.substring((keyword.first - 40).coerceAtLeast(0), keyword.first)
    val after = text.substring(keyword.last + 1, (keyword.last + 41).coerceAtMost(text.length))
    return qualifierBefore.containsMatchIn(before) || qualifierAfter.containsMatchIn(after)
  }

  /**
   * @param keywords where the upstream matcher found code keywords in [text], in order.
   * @return the first qualified keyword with a code shortly after it.
   */
  fun locate(text: String, keywords: List<IntRange>): Hit? {
    for (keyword in keywords) {
      val from = keyword.last + 1
      if (from < 0 || from > text.length || !qualifies(text, keyword)) continue
      val code =
          candidate
              .findAll(text, from)
              .mapNotNull { it.groups[1] }
              .takeWhile { it.range.first - from <= MAX_DISTANCE }
              .firstOrNull { !yearLike.matches(it.value) } ?: continue
      return Hit(keyword, code.range, code.value)
    }
    return null
  }
}
