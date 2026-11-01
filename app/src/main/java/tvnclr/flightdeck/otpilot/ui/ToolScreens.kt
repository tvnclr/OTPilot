package tvnclr.flightdeck.otpilot.ui

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
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdEmptyState
import tvnclr.flightdeck.ui.FdListPage
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdPage
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSearchField
import tvnclr.flightdeck.ui.FdSection
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdStatus
import tvnclr.flightdeck.ui.FdTextField
import tvnclr.flightdeck.ui.FdType
import tvnclr.flightdeck.ui.fdStatusColor

@Composable
fun DetectionTestScreen(upPress: () -> Unit, vm: DetectionTestViewModel) {
  var content by remember { mutableStateOf("") }
  var loaded by remember { mutableStateOf(false) }
  var ignoredPhrase by remember { mutableStateOf<String?>(null) }
  var cleaned by remember { mutableStateOf<String?>(null) }
  var detection by remember { mutableStateOf(Detection(null, null, null)) }
  val c = Fd.colors

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

  FdPage(title = stringResource(R.string.detection_test), subtitle = stringResource(R.string.detection_test_desc), onBack = upPress) {
    FdSection(stringResource(R.string.otp_test_input), Icons.Rounded.EditNote) {
      Column(Modifier.padding(FdSpace.m)) {
        FdTextField(content, { content = it }, placeholder = stringResource(R.string.otp_test_placeholder), singleLine = false, minHeight = 140)
      }
    }

    FdSection(stringResource(R.string.otp_test_result), Icons.Rounded.Key) {
      val ignored = !ignoredPhrase.isNullOrBlank()
      val code = detection.code
      when {
        content.isBlank() -> FdNote(stringResource(R.string.detection_test_empty_hint))
        ignored ->
            FdRow(
                stringResource(R.string.otp_test_ignored, ignoredPhrase!!),
                icon = Icons.Rounded.Block,
                iconTint = fdStatusColor(FdStatus.ERROR),
                iconBackground = fdStatusColor(FdStatus.ERROR).copy(alpha = 0.14f),
            )
        code != null ->
            FdRow(
                stringResource(R.string.otp_detail_code),
                icon = Icons.Rounded.CheckCircle,
                iconTint = fdStatusColor(FdStatus.OK),
                iconBackground = fdStatusColor(FdStatus.OK).copy(alpha = 0.14f),
                trailing = {
                  SelectionContainer { Text(code, style = FdType.code.copy(fontSize = FdType.title.fontSize), color = c.accent) }
                },
            )
        else ->
            FdRow(
                stringResource(R.string.otp_test_none),
                icon = Icons.Rounded.SearchOff,
                iconTint = c.textSecondary,
                iconBackground = c.elevated,
            )
      }
      val text = cleaned
      if (!ignored && !text.isNullOrBlank()) {
        FdDivider(inset = false)
        Column(Modifier.fillMaxWidth().padding(FdSpace.l), verticalArrangement = Arrangement.spacedBy(FdSpace.s)) {
          Text(stringResource(R.string.otp_test_after_cleanup), style = FdType.label, color = c.textSecondary)
          SelectionContainer { Text(highlighted(text, detection), style = FdType.body, color = c.textPrimary) }
          if (detection.codeRange != null) Text(stringResource(R.string.otp_detail_legend), style = FdType.caption, color = c.textSecondary)
        }
      }
    }
  }
}

@Composable
fun LanguageScreen(upPress: () -> Unit, vm: LanguageSelectionViewModel) {
  val ui by vm.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val c = Fd.colors
  var current by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { "default" }) }

  FdListPage(title = stringResource(R.string.language), subtitle = stringResource(R.string.otp_language_subtitle), onBack = upPress) {
    item(key = "search") { FdSearchField(ui.searchTerm, vm::setSearchTerm, stringResource(R.string.search_language)) }
    if (ui.locales.isEmpty()) {
      item(key = "empty") { FdCard { FdEmptyState(stringResource(R.string.no_result), Icons.Rounded.HelpOutline) } }
    } else {
      item(key = "list") {
        FdCard {
          ui.locales.forEachIndexed { i, locale ->
            if (i > 0) FdDivider(inset = false)
            val selected = current == locale.code
            FdRow(
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
