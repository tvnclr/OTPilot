package tvnclr.flightdeck.otpilot.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.screens.CleanupPhrasesViewModel
import io.github.jd1378.otphelper.ui.screens.IgnoredPhrasesViewModel
import io.github.jd1378.otphelper.ui.screens.SensitivePhrasesViewModel
import io.github.jd1378.otphelper.utils.Clipboard
import io.github.jd1378.otphelper.utils.openUriSafely
import kotlinx.coroutines.flow.MutableStateFlow
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdButton
import tvnclr.flightdeck.ui.FdButtonStyle
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdConfirmDialog
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdEmptyState
import tvnclr.flightdeck.ui.FdIconButton
import tvnclr.flightdeck.ui.FdInputDialog
import tvnclr.flightdeck.ui.FdListPage
import tvnclr.flightdeck.ui.FdMenuItem
import tvnclr.flightdeck.ui.FdOverflowMenu
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdType

private const val REGEX_HELP = "https://regextutorial.org/"

/** One screen for the three phrase lists (sensitive, ignored, cleanup). */
@Composable
private fun PhraseListScreen(
    title: String,
    description: String,
    newPhraseTitle: String,
    phrases: List<String>,
    showReset: MutableStateFlow<Boolean>,
    showClear: MutableStateFlow<Boolean>,
    showNew: MutableStateFlow<Boolean>,
    copyItems: List<Pair<String, () -> String>>,
    isValid: (String) -> Boolean,
    onReset: () -> Unit,
    onClear: () -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (Int) -> Unit,
    upPress: () -> Unit,
) {
  val context = LocalContext.current
  val uri = LocalUriHandler.current
  val reset by showReset.collectAsStateWithLifecycle()
  val clear by showClear.collectAsStateWithLifecycle()
  val new by showNew.collectAsStateWithLifecycle()
  val c = Fd.colors

  if (reset) {
    FdConfirmDialog(
        stringResource(R.string.reset_to_default), stringResource(R.string.irreversible_action_warning),
        stringResource(R.string.yes), stringResource(R.string.no), onReset, { showReset.value = false }, destructive = true,
    )
  }
  if (clear) {
    FdConfirmDialog(
        stringResource(R.string.clear_list), stringResource(R.string.irreversible_action_warning),
        stringResource(R.string.yes), stringResource(R.string.no), onClear, { showClear.value = false }, destructive = true,
    )
  }
  if (new) {
    FdInputDialog(
        title = newPhraseTitle,
        placeholder = stringResource(R.string.your_new_phrase),
        confirmText = stringResource(R.string.add),
        dismissText = stringResource(R.string.cancel),
        isValid = isValid,
        onConfirm = onAdd,
        onDismiss = { showNew.value = false },
    )
  }

  FdListPage(
      title = title,
      subtitle = stringResource(R.string.otp_phrases_count, phrases.size),
      onBack = upPress,
      actions = {
        FdOverflowMenu(
            listOf(
                FdMenuItem(stringResource(R.string.reset_to_default), Icons.Rounded.SettingsBackupRestore) { showReset.value = true },
                FdMenuItem(stringResource(R.string.clear_list), Icons.Outlined.Delete, destructive = true) { showClear.value = true },
            ) + copyItems.map { (label, regex) -> FdMenuItem(label, Icons.Rounded.ContentCopy) { Clipboard.copyToClipboard(context, regex(), false) } },
            stringResource(R.string.options),
        )
      },
      bottomBar = {
        FdButton(stringResource(R.string.otp_phrase_add), { showNew.value = true }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
      },
  ) {
    item(key = "desc") {
      FdCard {
        Column(Modifier.padding(FdSpace.l), verticalArrangement = Arrangement.spacedBy(FdSpace.m)) {
          Text(description, style = FdType.caption, color = c.textSecondary)
          Text(stringResource(R.string.sensitive_phrases_desc_regex), style = FdType.caption, color = c.textSecondary)
          FdButton(
              stringResource(R.string.learn_more),
              { uri.openUriSafely(context, REGEX_HELP) },
              icon = Icons.Rounded.OpenInNew,
              style = FdButtonStyle.TONAL,
          )
        }
      }
    }
    if (phrases.isEmpty()) {
      item(key = "empty") { FdCard { FdEmptyState(stringResource(R.string.list_is_empty), Icons.Rounded.FormatQuote) } }
    } else {
      item(key = "list") {
        FdCard {
          phrases.forEachIndexed { i, phrase ->
            if (i > 0) FdDivider(inset = false)
            FdRow(
                phrase,
                trailing = { FdIconButton(Icons.Rounded.Close, stringResource(R.string.delete), { onDelete(i) }) },
            )
          }
        }
      }
    }
  }
}

@Composable
fun SensitivePhrasesScreen(upPress: () -> Unit, vm: SensitivePhrasesViewModel) {
  val phrases by vm.sensitivePhrases.collectAsStateWithLifecycle()
  PhraseListScreen(
      title = stringResource(R.string.sensitive_phrases),
      description = stringResource(R.string.sensitive_phrases_desc),
      newPhraseTitle = stringResource(R.string.new_sensitive_phrase),
      phrases = phrases,
      showReset = vm.showResetToDefaultDialog,
      showClear = vm.showClearListDialog,
      showNew = vm.showNewSensitivePhraseDialog,
      copyItems =
          listOf(
              stringResource(R.string.copy_general_regex) to { vm.autoUpdatingListenerUtils.codeExtractor?.generalCodeMatcher?.toString() ?: "" },
              stringResource(R.string.copy_special_regex) to { vm.autoUpdatingListenerUtils.codeExtractor?.specialCodeMatcher?.toString() ?: "" },
          ),
      isValid = vm::isSensitivePhraseParsable,
      onReset = vm::resetToDefault,
      onClear = vm::clearList,
      onAdd = vm::addNewPhrase,
      onDelete = vm::deletePhrase,
      upPress = upPress,
  )
}

@Composable
fun IgnoredPhrasesScreen(upPress: () -> Unit, vm: IgnoredPhrasesViewModel) {
  val phrases by vm.ignoredPhrases.collectAsStateWithLifecycle()
  PhraseListScreen(
      title = stringResource(R.string.ignored_phrases),
      description = stringResource(R.string.ignored_phrases_desc),
      newPhraseTitle = stringResource(R.string.new_ignored_phrase),
      phrases = phrases,
      showReset = vm.showResetToDefaultDialog,
      showClear = vm.showClearListDialog,
      showNew = vm.showNewIgnoredPhraseDialog,
      copyItems = listOf(stringResource(R.string.copy_ignore_regex) to { vm.autoUpdatingListenerUtils.codeExtractor?.ignoredPhrasesRegex?.toString() ?: "" }),
      isValid = vm::isIgnoredPhraseParsable,
      onReset = vm::resetToDefault,
      onClear = vm::clearList,
      onAdd = vm::addNewPhrase,
      onDelete = vm::deletePhrase,
      upPress = upPress,
  )
}

@Composable
fun CleanupPhrasesScreen(upPress: () -> Unit, vm: CleanupPhrasesViewModel) {
  val phrases by vm.cleanupPhrases.collectAsStateWithLifecycle()
  PhraseListScreen(
      title = stringResource(R.string.cleanup_phrases),
      description = stringResource(R.string.cleanup_phrases_desc),
      newPhraseTitle = stringResource(R.string.new_cleanup_phrase),
      phrases = phrases,
      showReset = vm.showResetToDefaultDialog,
      showClear = vm.showClearListDialog,
      showNew = vm.showNewCleanupPhraseDialog,
      copyItems = listOf(stringResource(R.string.copy_cleanup_regex) to { vm.autoUpdatingListenerUtils.codeExtractor?.cleanupPhrasesRegex?.toString() ?: "" }),
      isValid = vm::isCleanupPhraseParsable,
      onReset = vm::resetToDefault,
      onClear = vm::clearList,
      onAdd = vm::addNewPhrase,
      onDelete = vm::deletePhrase,
      upPress = upPress,
  )
}
