package dev.tvnclr.otpilot.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
import dev.tvnclr.ui.Fd
import dev.tvnclr.ui.TvButton
import dev.tvnclr.ui.TvButtonStyle
import dev.tvnclr.ui.TvCard
import dev.tvnclr.ui.TvConfirmDialog
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvEmptyState
import dev.tvnclr.ui.TvInputDialog
import dev.tvnclr.ui.TvListPage
import dev.tvnclr.ui.TvMenuItem
import dev.tvnclr.ui.TvOverflowMenu
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvType

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
  val c = Tv.colors

  if (reset) {
    TvConfirmDialog(
        stringResource(R.string.reset_to_default), stringResource(R.string.irreversible_action_warning),
        stringResource(R.string.yes), stringResource(R.string.no), onReset, { showReset.value = false }, destructive = true,
    )
  }
  if (clear) {
    TvConfirmDialog(
        stringResource(R.string.clear_list), stringResource(R.string.irreversible_action_warning),
        stringResource(R.string.yes), stringResource(R.string.no), onClear, { showClear.value = false }, destructive = true,
    )
  }
  if (new) {
    TvInputDialog(
        title = newPhraseTitle,
        placeholder = stringResource(R.string.your_new_phrase),
        confirmText = stringResource(R.string.add),
        dismissText = stringResource(R.string.cancel),
        isValid = isValid,
        onConfirm = onAdd,
        onDismiss = { showNew.value = false },
    )
  }

  TvListPage(
      title = title,
      subtitle = stringResource(R.string.otp_phrases_count, phrases.size),
      onBack = upPress,
      actions = {
        TvOverflowMenu(
            listOf(
                TvMenuItem(stringResource(R.string.reset_to_default), Icons.Rounded.SettingsBackupRestore) { showReset.value = true },
                TvMenuItem(stringResource(R.string.clear_list), Icons.Outlined.Delete, destructive = true) { showClear.value = true },
            ) + copyItems.map { (label, regex) -> TvMenuItem(label, Icons.Rounded.ContentCopy) { Clipboard.copyToClipboard(context, regex(), false) } },
            stringResource(R.string.options),
        )
      },
      bottomBar = {
        TvButton(stringResource(R.string.otp_phrase_add), { showNew.value = true }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
      },
  ) {
    item(key = "desc") {
      TvCard {
        Column(Modifier.padding(TvSpace.l), verticalArrangement = Arrangement.spacedBy(TvSpace.m)) {
          Text(description, style = TvType.caption, color = c.textSecondary)
          Text(stringResource(R.string.sensitive_phrases_desc_regex), style = TvType.caption, color = c.textSecondary)
          TvButton(
              stringResource(R.string.learn_more),
              { uri.openUriSafely(context, REGEX_HELP) },
              icon = Icons.Rounded.OpenInNew,
              style = TvButtonStyle.TONAL,
          )
        }
      }
    }
    if (phrases.isEmpty()) {
      item(key = "empty") { TvCard { TvEmptyState(stringResource(R.string.list_is_empty), Icons.Rounded.FormatQuote) } }
    } else {
      item(key = "list") {
        TvCard {
          phrases.forEachIndexed { i, phrase ->
            if (i > 0) TvDivider(inset = false)
            PhraseRow(phrase, stringResource(R.string.delete)) { onDelete(i) }
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

/** Compact row for a regex phrase: monospace text and a small delete button. */
@Composable
private fun PhraseRow(phrase: String, deleteLabel: String, onDelete: () -> Unit) {
  val c = Tv.colors
  Row(
      Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = TvSpace.l, end = TvSpace.xs),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(phrase, style = TvType.mono.copy(fontSize = TvType.body.fontSize), color = c.textPrimary, modifier = Modifier.weight(1f).padding(vertical = TvSpace.s))
    Box(
        Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = deleteLabel, onClick = onDelete),
        contentAlignment = Alignment.Center,
    ) {
      Icon(Icons.Rounded.Close, deleteLabel, tint = c.textSecondary, modifier = Modifier.size(20.dp))
    }
  }
}
