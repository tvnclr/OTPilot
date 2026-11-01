package tvnclr.flightdeck.otpilot

/**
 * OTPilot context fallback.
 *
 * Runs only when the upstream matchers found a code keyword ("code", "código", ...) but no code
 * next to it, e.g. "use the verification code below to proceed ... process. 316974 Thank you".
 * It picks the first standalone 4-8 digit number that appears AFTER the keyword, skipping
 * numbers that look like dates, times, amounts, years, phone numbers or references.
 * Nothing upstream already detects is changed: this only runs where upstream returns null.
 */
object ContextFallback {
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

  /** @param keywordEnd index right after the keyword the upstream matcher found. */
  fun find(text: String, keywordEnd: Int): String? {
    if (keywordEnd < 0 || keywordEnd > text.length) return null
    return candidate
        .findAll(text, keywordEnd)
        .map { it.groupValues[1] }
        .firstOrNull { !yearLike.matches(it) }
  }
}
