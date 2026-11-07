package dev.tvnclr.otpilot.ui

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
import androidx.compose.material.icons.rounded.Visibility
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
import dev.tvnclr.ui.Fd
import dev.tvnclr.ui.TvAccent
import dev.tvnclr.ui.TvCodeBlock
import dev.tvnclr.ui.TvColorDots
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvRow
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSegmented
import dev.tvnclr.ui.TvShape
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvSwitchRow
import dev.tvnclr.ui.TvThemeMode
import dev.tvnclr.ui.TvThemePrefs
import dev.tvnclr.ui.TvThemeSettings
import dev.tvnclr.ui.TvType
import dev.tvnclr.ui.isDynamicColorAvailable

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
  val theme by remember { TvThemePrefs.flow(context) }.collectAsState(initial = TvThemeSettings())
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

  TvPage(title = stringResource(R.string.settings), subtitle = stringResource(R.string.otp_settings_subtitle), onBack = upPress) {
    // ── Appearance ──
    TvSection(stringResource(R.string.otp_appearance), Icons.Rounded.Palette) {
      Column(Modifier.padding(horizontal = TvSpace.l, vertical = TvSpace.m)) {
        Text(stringResource(R.string.otp_theme), style = TvType.item, color = Tv.colors.textPrimary)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = TvSpace.s))
        val modes = TvThemeMode.entries
        TvSegmented(
            options =
                listOf(
                    stringResource(R.string.otp_theme_system),
                    stringResource(R.string.otp_theme_light),
                    stringResource(R.string.otp_theme_dark),
                    stringResource(R.string.otp_theme_amoled),
                ),
            selected = modes.indexOf(theme.mode),
            onSelect = { i -> scope.launch { TvThemePrefs.setMode(context, modes[i]) } },
        )
      }
      TvDivider(inset = false)
      Column(Modifier.padding(top = TvSpace.m)) {
        Text(
            stringResource(R.string.otp_accent),
            style = TvType.item,
            color = Tv.colors.textPrimary,
            modifier = Modifier.padding(horizontal = TvSpace.l),
        )
        val accents = TvAccent.entries.filter { it != TvAccent.DYNAMIC || isDynamicColorAvailable() }
        val dark = Tv.colors.isDark
        TvColorDots(
            colors = accents.map { if (dark) it.dark else it.light },
            selected = accents.indexOf(theme.accent),
            onSelect = { i -> scope.launch { TvThemePrefs.setAccent(context, accents[i]) } },
            dynamicIndex = accents.indexOf(TvAccent.DYNAMIC),
            labels = accents.map { it.name },
        )
        if (theme.accent == TvAccent.DYNAMIC) {
          TvNote(stringResource(R.string.otp_accent_dynamic))
        }
      }
      TvDivider(inset = false)
      Column(Modifier.padding(horizontal = TvSpace.l, vertical = TvSpace.m)) {
        Text(stringResource(R.string.otp_bg_title), style = TvType.item, color = Tv.colors.textPrimary)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = TvSpace.s))
        val styles = dev.tvnclr.ui.TvBackgroundStyle.entries
        dev.tvnclr.ui.TvChoiceChips(
            options = styles.map { stringResource(bgLabel(it)) },
            selected = styles.indexOf(theme.background),
            onSelect = { i -> scope.launch { TvThemePrefs.setBackground(context, styles[i]) } },
        )
        if (!android.animation.ValueAnimator.areAnimatorsEnabled() && theme.background != dev.tvnclr.ui.TvBackgroundStyle.PLAIN) {
          Text(stringResource(R.string.otp_bg_reduced_motion), style = TvType.caption, color = Tv.colors.textSecondary, modifier = Modifier.padding(top = TvSpace.s))
        }
      }
    }

    // ── Copying ──
    TvSection(stringResource(R.string.otp_copying), Icons.Rounded.ContentCopy) {
      TvSwitchRow(stringResource(R.string.auto_copy), s.isAutoCopyEnabled, { vm.onAutoCopyToggle() }, stringResource(R.string.auto_copy_help), Icons.Rounded.ContentCopy)
      TvDivider()
      TvSwitchRow(
          stringResource(R.string.show_copy_confirmation),
          s.isShowCopyConfirmationEnabled,
          { vm.onShowCopyConfirmationToggle() },
          stringResource(R.string.show_confirmation_help, stringResource(R.string.code_copied_to_clipboard)),
          Icons.Rounded.DoneAll,
      )
      TvDivider()
      TvSwitchRow(stringResource(R.string.show_toast), s.isShowToastEnabled, { vm.onShowToastToggle() }, stringResource(R.string.show_toast_help), Icons.Rounded.Campaign)
      TvDivider()
      TvSwitchRow(stringResource(R.string.send_detected_notif), s.isPostNotifEnabled, { vm.onPostNotifToggle() }, stringResource(R.string.send_detected_notif_help), Icons.Rounded.Notifications)
      TvDivider()
      TvSwitchRow(stringResource(R.string.copy_as_not_sensitive), s.isCopyAsNotSensitiveEnabled, { vm.onIsCopyAsNotSensitiveToggle() }, stringResource(R.string.copy_as_not_sensitive_desc), Icons.Rounded.VisibilityOff)
      TvDivider()
      TvRow(stringResource(R.string.send_test_notification), icon = Icons.Rounded.Send, showChevron = false, onClick = { vm.onSendTestNotifPressed(context) })
    }

    // ── Detection ──
    TvSection(stringResource(R.string.otp_detection), Icons.Rounded.ManageSearch) {
      TvRow(stringResource(R.string.sensitive_phrases), stringResource(R.string.otp_sensitive_desc), Icons.Rounded.Key, onClick = { go(MainDestinations.SENSITIVE_PHRASES_ROUTE) })
      TvDivider()
      TvRow(stringResource(R.string.ignored_phrases), stringResource(R.string.otp_ignored_phrases_desc), Icons.Rounded.Block, onClick = { go(MainDestinations.IGNORED_PHRASES_ROUTE) })
      TvDivider()
      TvRow(stringResource(R.string.cleanup_phrases), stringResource(R.string.otp_cleanup_desc), Icons.Rounded.CleaningServices, onClick = { go(MainDestinations.CLEANUP_PHRASES_ROUTE) })
      TvDivider()
      TvRow(stringResource(R.string.detection_test), stringResource(R.string.otp_detection_test_desc), Icons.Rounded.Science, onClick = { go(MainDestinations.DETECTION_TEST_ROUTE) })
    }

    // ── History ──
    TvSection(stringResource(R.string.otp_history_section), Icons.Rounded.History) {
      TvSwitchRow(stringResource(R.string.history), !s.isHistoryDisabled, { vm.onHistoryToggle(context) }, stringResource(R.string.history_help), Icons.Rounded.History)
      TvDivider()
      TvSwitchRow(
          stringResource(R.string.replace_code_in_history),
          s.shouldReplaceCodeInHistory,
          { vm.onShouldReplaceCodeInHistoryToggle() },
          stringResource(R.string.replace_code_in_history_help),
          Icons.Rounded.SwapHoriz,
          enabled = !s.isHistoryDisabled,
      )
    }

    // ── Notification actions (experimental) ──
    TvSection(stringResource(R.string.otp_notif_actions), Icons.Rounded.NotificationsActive) {
      TvNote(stringResource(R.string.experimental_features_warning))
      TvDivider(inset = false)
      TvSwitchRow(stringResource(R.string.auto_dismiss), s.isAutoDismissEnabled, { vm.onAutoDismissToggle() }, stringResource(R.string.auto_dismiss_desc), Icons.Rounded.ClearAll)
      TvDivider()
      TvSwitchRow(stringResource(R.string.auto_mark_as_read), s.isAutoMarkAsReadEnabled, { vm.onAutoMarkAsReadToggle() }, stringResource(R.string.auto_mark_as_read_help), Icons.Rounded.DoneAll)
    }

    // ── Integrations ──
    TvSection(stringResource(R.string.otp_integrations), Icons.Rounded.Hub) {
      TvSwitchRow(stringResource(R.string.broadcast_code), s.isBroadcastCodeEnabled, { vm.onBroadcastCodeToggle() }, stringResource(R.string.broadcast_code_help), Icons.Rounded.Hub)
      Column(Modifier.fillMaxWidth().padding(horizontal = TvSpace.l, vertical = TvSpace.s)) {
        val c = Tv.colors
        OutlinedTextField(
            value = targetPackage,
            onValueChange = { targetPackage = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.target_package_name)) },
            placeholder = { Text("com.llamalab.automate") },
            supportingText = { Text(stringResource(R.string.broadcast_target_help), style = TvType.caption) },
            singleLine = true,
            shape = TvShape.inner,
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
        Text(stringResource(R.string.otp_intent_action), style = TvType.label, color = c.textSecondary, modifier = Modifier.padding(top = TvSpace.s, bottom = TvSpace.xs))
        TvCodeBlock(stringResource(R.string.intent_action_code_detected))
        Text(stringResource(R.string.intent_action_code_detected_help), style = TvType.caption, color = c.textSecondary, modifier = Modifier.padding(vertical = TvSpace.s))
      }
    }

    // ── General ──
    TvSection(stringResource(R.string.otp_general), Icons.Rounded.Tune) {
      TvRow(
          stringResource(R.string.language),
          getCurrentLocale()?.displayLanguage ?: stringResource(R.string.error),
          Icons.Rounded.Language,
          onClick = { go(MainDestinations.LANGUAGE_SELECTION_ROUTE) },
      )
      if (dev.tvnclr.otpilot.OtpilotPrefs.recentsPreviewSupported) {
        val preview by remember { dev.tvnclr.otpilot.OtpilotPrefs.recentsPreview(context) }.collectAsState(initial = false)
        TvDivider()
        TvSwitchRow(
            stringResource(R.string.otp_recents_preview),
            preview,
            { scope.launch { dev.tvnclr.otpilot.OtpilotPrefs.setRecentsPreview(context, it) } },
            subtitle = stringResource(R.string.otp_recents_preview_desc),
            icon = Icons.Rounded.Visibility,
        )
      }
      TvDivider()
      TvRow(stringResource(R.string.about), stringResource(R.string.otp_about_desc), Icons.Rounded.Info, onClick = { go(MainDestinations.ABOUT_ROUTE) })
    }
  }
}

private fun bgLabel(style: dev.tvnclr.ui.TvBackgroundStyle): Int =
    when (style) {
      dev.tvnclr.ui.TvBackgroundStyle.ORBS -> R.string.otp_bg_orbs
      dev.tvnclr.ui.TvBackgroundStyle.AURORA -> R.string.otp_bg_aurora
      dev.tvnclr.ui.TvBackgroundStyle.HORIZON -> R.string.otp_bg_horizon
      dev.tvnclr.ui.TvBackgroundStyle.RADAR -> R.string.otp_bg_radar
      dev.tvnclr.ui.TvBackgroundStyle.DEPTH -> R.string.otp_bg_depth
      dev.tvnclr.ui.TvBackgroundStyle.PLAIN -> R.string.otp_bg_plain
    }
