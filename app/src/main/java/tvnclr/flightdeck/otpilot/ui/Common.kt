package tvnclr.flightdeck.otpilot.ui

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.jd1378.otphelper.ui.components.AppImage
import io.github.jd1378.otphelper.ui.components.getAppInfo
import io.github.jd1378.otphelper.utils.CodeExtractor
import java.util.Date
import tvnclr.flightdeck.otpilot.ContextFallback
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdShape
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdStatus
import tvnclr.flightdeck.ui.FdType
import tvnclr.flightdeck.ui.fdStatusColor

/** Compact pill button used as the trailing action of a row ("Grant", "Open"...). */
@Composable
fun RowAction(text: String, onClick: () -> Unit, enabled: Boolean = true, color: Color = Fd.colors.accent) {
  Surface(
      onClick = onClick,
      enabled = enabled,
      shape = FdShape.pill,
      color = color.copy(alpha = 0.14f),
      border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
  ) {
    Text(
        text,
        style = FdType.label,
        color = color,
        modifier = Modifier.heightIn(min = 32.dp).padding(horizontal = 14.dp, vertical = 8.dp),
    )
  }
}

/** App icon in the badge shape, same footprint as FdIconBadge. */
@Composable
fun AppBadge(packageName: String?, size: androidx.compose.ui.unit.Dp = 40.dp) {
  val context = LocalContext.current
  val info = remember(packageName) { getAppInfo(context, packageName) }
  AppImage(info.icon, Modifier.size(size).clip(FdShape.badge))
}

/** Clickable card with an app icon, title, subtitle and chevron (history, ignored apps). */
@Composable
fun AppCardRow(packageName: String?, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val c = Fd.colors
  FdCard(modifier, onClick = onClick) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = FdSpace.l, vertical = FdSpace.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FdSpace.m),
    ) {
      AppBadge(packageName)
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FdSpace.xxs)) {
        Text(title, style = FdType.item, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, style = FdType.caption, color = c.accent)
      }
      Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.textSecondary, modifier = Modifier.size(24.dp))
    }
  }
}

@Composable
fun FdLoading(modifier: Modifier = Modifier) {
  Box(modifier.fillMaxWidth().padding(FdSpace.xxl), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = Fd.colors.accent, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
  }
}

fun relativeTime(date: Date): String =
    DateUtils.getRelativeTimeSpanString(date.time, System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL).toString()

/** Where the keyword and the code are inside [text], as OTPilot actually detects them. */
data class Detection(val code: String?, val phrase: IntRange?, val codeRange: IntRange?)

/**
 * Runs the same detection the listeners use ([CodeExtractor.getCode], which includes the OTPilot
 * fallback) and locates keyword/code in [text] for highlighting. [text] must already be cleaned.
 */
fun detect(extractor: CodeExtractor?, text: String?): Detection {
  if (extractor == null || text.isNullOrEmpty()) return Detection(null, null, null)
  val code = extractor.getCode(text, doCleanup = false)
  val match = extractor.getCodeMatch(text)
  if (match != null) {
    val phrase = match.matchResult.groups[match.phraseGroup]?.range
    val codeRange = match.matchResult.groups[match.codeGroup]?.range
    return Detection(code, phrase, codeRange)
  }
  if (code == null) return Detection(null, null, null)
  // fallback path: keyword found, code further ahead
  val keyword = extractor.generalCodeMatcher.find(text)?.groups?.get(1)?.range
  val from = (keyword?.last ?: -1) + 1
  return Detection(code, keyword, ContextFallback.findRange(text, from))
}

@Composable
fun highlighted(text: String, d: Detection): AnnotatedString {
  val c = Fd.colors
  val phraseColor = fdStatusColor(FdStatus.WARN)
  return buildAnnotatedString {
    append(text)
    d.phrase?.let {
      if (it.last < text.length) addStyle(SpanStyle(color = phraseColor, fontWeight = FontWeight.Bold), it.first, it.last + 1)
    }
    d.codeRange?.let {
      if (it.last < text.length) addStyle(SpanStyle(color = c.accent, background = c.accentSoft, fontWeight = FontWeight.ExtraBold), it.first, it.last + 1)
    }
  }
}

/** Centered content for full-screen states (empty list, error). */
@Composable
fun CenterBox(content: @Composable () -> Unit) {
  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
