package dev.tvnclr.otpilot.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.screens.DetectionTestViewModel
import io.github.jd1378.otphelper.ui.screens.LanguageSelectionViewModel
import kotlinx.coroutines.delay
import dev.tvnclr.ui.Fd
import dev.tvnclr.ui.TvCard
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvEmptyState
import dev.tvnclr.ui.TvListPage
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvRow
import dev.tvnclr.ui.TvSearchField
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvStatus
import dev.tvnclr.ui.TvTextField
import dev.tvnclr.ui.TvType
import dev.tvnclr.ui.tvStatusColor

@Composable
fun DetectionTestScreen(upPress: () -> Unit, vm: DetectionTestViewModel) {
  var content by remember { mutableStateOf("") }
  var loaded by remember { mutableStateOf(false) }
  var ignoredPhrase by remember { mutableStateOf<String?>(null) }
  var cleaned by remember { mutableStateOf<String?>(null) }
  var detection by remember { mutableStateOf(Detection(null, null, null)) }
  val c = Tv.colors

  LaunchedEffect(Unit) {
    content = vm.getSavedDetectionTestContent()
    loaded = true
  }
  LaunchedEffect(content, loaded) {
    if (!loaded) return@LaunchedEffect
    delay(300) // debounce
    vm.saveDetectionTestContent(content)
    val ex = vm.autoUpdatingListenerUtils.codeExtractor ?: return@LaunchedEffect
    ignoredPhrase = ex.getIgnorePhrase(content)
    cleaned = ex.cleanup(content)
    detection = detect(ex, cleaned)
  }

  TvPage(title = stringResource(R.string.detection_test), subtitle = stringResource(R.string.detection_test_desc), onBack = upPress) {
    TvSection(stringResource(R.string.otp_test_input), Icons.Rounded.EditNote) {
      Column(Modifier.padding(TvSpace.m)) {
        TvTextField(content, { content = it }, placeholder = stringResource(R.string.otp_test_placeholder), singleLine = false, minHeight = 140)
      }
    }

    TvSection(stringResource(R.string.otp_test_result), Icons.Rounded.Key) {
      val ignored = !ignoredPhrase.isNullOrBlank()
      val code = detection.code
      when {
        content.isBlank() -> TvNote(stringResource(R.string.detection_test_empty_hint))
        ignored ->
            TvRow(
                stringResource(R.string.otp_test_ignored, ignoredPhrase!!),
                icon = Icons.Rounded.Block,
                iconTint = tvStatusColor(TvStatus.ERROR),
                iconBackground = tvStatusColor(TvStatus.ERROR).copy(alpha = 0.14f),
            )
        code != null ->
            TvRow(
                stringResource(R.string.otp_detail_code),
                icon = Icons.Rounded.CheckCircle,
                iconTint = tvStatusColor(TvStatus.OK),
                iconBackground = tvStatusColor(TvStatus.OK).copy(alpha = 0.14f),
                trailing = {
                  SelectionContainer { Text(code, style = TvType.code.copy(fontSize = TvType.title.fontSize), color = c.accent) }
                },
            )
        else ->
            TvRow(
                stringResource(R.string.otp_test_none),
                icon = Icons.Rounded.SearchOff,
                iconTint = c.textSecondary,
                iconBackground = c.elevated,
            )
      }
      val text = cleaned
      if (!ignored && !text.isNullOrBlank()) {
        TvDivider(inset = false)
        Column(Modifier.fillMaxWidth().padding(TvSpace.l), verticalArrangement = Arrangement.spacedBy(TvSpace.s)) {
          Text(stringResource(R.string.otp_test_after_cleanup), style = TvType.label, color = c.textSecondary)
          SelectionContainer { Text(highlighted(text, detection), style = TvType.body, color = c.textPrimary) }
          if (detection.codeRange != null) Text(stringResource(R.string.otp_detail_legend), style = TvType.caption, color = c.textSecondary)
        }
      }
    }
  }
}

@Composable
fun LanguageScreen(upPress: () -> Unit, vm: LanguageSelectionViewModel) {
  val ui by vm.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val c = Tv.colors
  var current by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { "default" }) }

  TvListPage(title = stringResource(R.string.language), subtitle = stringResource(R.string.otp_language_subtitle), onBack = upPress) {
    item(key = "search") { TvSearchField(ui.searchTerm, vm::setSearchTerm, stringResource(R.string.search_language)) }
    if (ui.locales.isEmpty()) {
      item(key = "empty") { TvCard { TvEmptyState(stringResource(R.string.no_result), Icons.Rounded.HelpOutline) } }
    } else {
      item(key = "list") {
        TvCard {
          ui.locales.forEachIndexed { i, locale ->
            if (i > 0) TvDivider(inset = false)
            val selected = current == locale.code
            TvRow(
                stringResource(locale.label),
                onClick = {
                  current = locale.code
                  vm.selectLocale(locale, context)
                },
                showChevron = false,
                trailing = { if (selected) Icon(Icons.Rounded.CheckCircle, null, tint = c.accent) },
            )
          }
        }
      }
    }
  }
}
