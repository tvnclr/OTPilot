package tvnclr.flightdeck.otpilot.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.ui.screens.SettingsViewModel
import io.github.jd1378.otphelper.ui.utils.getCurrentLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdAccent
import tvnclr.flightdeck.ui.FdCodeBlock
import tvnclr.flightdeck.ui.FdColorDots
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdPage
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSection
import tvnclr.flightdeck.ui.FdSegmented
import tvnclr.flightdeck.ui.FdShape
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdSwitchRow
import tvnclr.flightdeck.ui.FdThemeMode
import tvnclr.flightdeck.ui.FdThemePrefs
import tvnclr.flightdeck.ui.FdThemeSettings
import tvnclr.flightdeck.ui.FdType
import tvnclr.flightdeck.ui.isDynamicColorAvailable

@Composable
fun SettingsScreen(
    onNavigateToRoute: (String, Boolean, Boolean) -> Unit,
    upPress: () -> Unit,
    vm: SettingsViewModel,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val focus = LocalFocusManager.current
  val s by vm.userSettings.collectAsState()
  val theme by remember { FdThemePrefs.flow(context) }.collectAsState(initial = FdThemeSettings())
  val go: (String) -> Unit = { onNavigateToRoute(it, false, true) }

  var targetPackage by remember { mutableStateOf("") }
  var loaded by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    targetPackage = vm.getBroadcastTargetPackageName()
    loaded = true
  }
  LaunchedEffect(targetPackage) {
    if (!loaded) return@LaunchedEffect
    delay(150)
    vm.setBroadcastTargetPackageName(targetPackage)
  }

  FdPage(title = stringResource(R.string.settings), subtitle = stringResource(R.string.otp_settings_subtitle), onBack = upPress) {
    // ── Appearance ──
    FdSection(stringResource(R.string.otp_appearance), Icons.Rounded.Palette) {
      Column(Modifier.padding(horizontal = FdSpace.l, vertical = FdSpace.m)) {
        Text(stringResource(R.string.otp_theme), style = FdType.item, color = Fd.colors.textPrimary)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = FdSpace.s))
        val modes = FdThemeMode.entries
        FdSegmented(
            options =
                listOf(
                    stringResource(R.string.otp_theme_system),
                    stringResource(R.string.otp_theme_light),
                    stringResource(R.string.otp_theme_dark),
                    stringResource(R.string.otp_theme_amoled),
                ),
            selected = modes.indexOf(theme.mode),
            onSelect = { i -> scope.launch { FdThemePrefs.setMode(context, modes[i]) } },
        )
      }
      FdDivider(inset = false)
      Column(Modifier.padding(top = FdSpace.m)) {
        Text(
            stringResource(R.string.otp_accent),
            style = FdType.item,
            color = Fd.colors.textPrimary,
            modifier = Modifier.padding(horizontal = FdSpace.l),
        )
        val accents = FdAccent.entries.filter { it != FdAccent.DYNAMIC || isDynamicColorAvailable() }
        val dark = Fd.colors.isDark
        FdColorDots(
            colors = accents.map { if (dark) it.dark else it.light },
            selected = accents.indexOf(theme.accent),
            onSelect = { i -> scope.launch { FdThemePrefs.setAccent(context, accents[i]) } },
            dynamicIndex = accents.indexOf(FdAccent.DYNAMIC),
            labels = accents.map { it.name },
        )
        if (theme.accent == FdAccent.DYNAMIC) {
          FdNote(stringResource(R.string.otp_accent_dynamic))
        }
      }
    }

    // ── Copying ──
    FdSection(stringResource(R.string.otp_copying), Icons.Rounded.ContentCopy) {
      FdSwitchRow(stringResource(R.string.auto_copy), s.isAutoCopyEnabled, { vm.onAutoCopyToggle() }, stringResource(R.string.auto_copy_help), Icons.Rounded.ContentCopy)
      FdDivider()
      FdSwitchRow(
          stringResource(R.string.show_copy_confirmation),
          s.isShowCopyConfirmationEnabled,
          { vm.onShowCopyConfirmationToggle() },
          stringResource(R.string.show_confirmation_help, stringResource(R.string.code_copied_to_clipboard)),
          Icons.Rounded.DoneAll,
      )
      FdDivider()
      FdSwitchRow(stringResource(R.string.show_toast), s.isShowToastEnabled, { vm.onShowToastToggle() }, stringResource(R.string.show_toast_help), Icons.Rounded.Campaign)
      FdDivider()
      FdSwitchRow(stringResource(R.string.send_detected_notif), s.isPostNotifEnabled, { vm.onPostNotifToggle() }, stringResource(R.string.send_detected_notif_help), Icons.Rounded.Notifications)
      FdDivider()
      FdSwitchRow(stringResource(R.string.copy_as_not_sensitive), s.isCopyAsNotSensitiveEnabled, { vm.onIsCopyAsNotSensitiveToggle() }, stringResource(R.string.copy_as_not_sensitive_desc), Icons.Rounded.VisibilityOff)
      FdDivider()
      FdRow(stringResource(R.string.send_test_notification), icon = Icons.Rounded.Send, showChevron = false, onClick = { vm.onSendTestNotifPressed(context) })
    }

    // ── Detection ──
    FdSection(stringResource(R.string.otp_detection), Icons.Rounded.ManageSearch) {
      FdRow(stringResource(R.string.sensitive_phrases), stringResource(R.string.otp_sensitive_desc), Icons.Rounded.Key, onClick = { go(MainDestinations.SENSITIVE_PHRASES_ROUTE) })
      FdDivider()
      FdRow(stringResource(R.string.ignored_phrases), stringResource(R.string.otp_ignored_phrases_desc), Icons.Rounded.Block, onClick = { go(MainDestinations.IGNORED_PHRASES_ROUTE) })
      FdDivider()
      FdRow(stringResource(R.string.cleanup_phrases), stringResource(R.string.otp_cleanup_desc), Icons.Rounded.CleaningServices, onClick = { go(MainDestinations.CLEANUP_PHRASES_ROUTE) })
      FdDivider()
      FdRow(stringResource(R.string.detection_test), stringResource(R.string.otp_detection_test_desc), Icons.Rounded.Science, onClick = { go(MainDestinations.DETECTION_TEST_ROUTE) })
    }

    // ── History ──
    FdSection(stringResource(R.string.otp_history_section), Icons.Rounded.History) {
      FdSwitchRow(stringResource(R.string.history), !s.isHistoryDisabled, { vm.onHistoryToggle(context) }, stringResource(R.string.history_help), Icons.Rounded.History)
      FdDivider()
      FdSwitchRow(
          stringResource(R.string.replace_code_in_history),
          s.shouldReplaceCodeInHistory,
          { vm.onShouldReplaceCodeInHistoryToggle() },
          stringResource(R.string.replace_code_in_history_help),
          Icons.Rounded.SwapHoriz,
          enabled = !s.isHistoryDisabled,
      )
    }

    // ── Notification actions (experimental) ──
    FdSection(stringResource(R.string.otp_notif_actions), Icons.Rounded.NotificationsActive) {
      FdNote(stringResource(R.string.experimental_features_warning))
      FdDivider(inset = false)
      FdSwitchRow(stringResource(R.string.auto_dismiss), s.isAutoDismissEnabled, { vm.onAutoDismissToggle() }, stringResource(R.string.auto_dismiss_desc), Icons.Rounded.ClearAll)
      FdDivider()
      FdSwitchRow(stringResource(R.string.auto_mark_as_read), s.isAutoMarkAsReadEnabled, { vm.onAutoMarkAsReadToggle() }, stringResource(R.string.auto_mark_as_read_help), Icons.Rounded.DoneAll)
    }

    // ── Integrations ──
    FdSection(stringResource(R.string.otp_integrations), Icons.Rounded.Hub) {
      FdSwitchRow(stringResource(R.string.broadcast_code), s.isBroadcastCodeEnabled, { vm.onBroadcastCodeToggle() }, stringResource(R.string.broadcast_code_help), Icons.Rounded.Hub)
      Column(Modifier.fillMaxWidth().padding(horizontal = FdSpace.l, vertical = FdSpace.s)) {
        val c = Fd.colors
        OutlinedTextField(
            value = targetPackage,
            onValueChange = { targetPackage = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.target_package_name)) },
            placeholder = { Text("com.llamalab.automate") },
            supportingText = { Text(stringResource(R.string.broadcast_target_help), style = FdType.caption) },
            singleLine = true,
            shape = FdShape.inner,
            enabled = s.isBroadcastCodeEnabled,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.accent,
                    unfocusedBorderColor = c.cardBorder,
                    focusedLabelColor = c.accent,
                    cursorColor = c.accent,
                ),
        )
        Text(stringResource(R.string.otp_intent_action), style = FdType.label, color = c.textSecondary, modifier = Modifier.padding(top = FdSpace.s, bottom = FdSpace.xs))
        FdCodeBlock(stringResource(R.string.intent_action_code_detected))
        Text(stringResource(R.string.intent_action_code_detected_help), style = FdType.caption, color = c.textSecondary, modifier = Modifier.padding(vertical = FdSpace.s))
      }
    }

    // ── General ──
    FdSection(stringResource(R.string.otp_general), Icons.Rounded.Tune) {
      FdRow(
          stringResource(R.string.language),
          getCurrentLocale()?.displayLanguage ?: stringResource(R.string.error),
          Icons.Rounded.Language,
          onClick = { go(MainDestinations.LANGUAGE_SELECTION_ROUTE) },
      )
      FdDivider()
      FdRow(stringResource(R.string.about), stringResource(R.string.otp_about_desc), Icons.Rounded.Info, onClick = { go(MainDestinations.ABOUT_ROUTE) })
    }
  }
}
