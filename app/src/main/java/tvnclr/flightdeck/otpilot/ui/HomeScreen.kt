package tvnclr.flightdeck.otpilot.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.jd1378.otphelper.ModeOfOperation
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.components.getAppInfo
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.utils.Clipboard
import io.github.jd1378.otphelper.utils.CodeExtractor
import tvnclr.flightdeck.otpilot.SensitiveNotifAccess
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdButton
import tvnclr.flightdeck.ui.FdButtonStyle
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdIconBadge
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdPage
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSection
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdStatus
import tvnclr.flightdeck.ui.FdStatusPill
import tvnclr.flightdeck.ui.FdType
import tvnclr.flightdeck.ui.fdStatusColor

@Composable
fun HomeScreen(onNavigateToRoute: (String, Boolean, Boolean) -> Unit, vm: OtpHomeViewModel) {
  val context = LocalContext.current
  val settings by vm.userSettings.collectAsStateWithLifecycle()
  val checks by vm.checks.collectAsStateWithLifecycle()
  val lastCodes = vm.lastCodes.collectAsLazyPagingItems()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh(context) }
  LaunchedEffect(settings) {
    if (!settings.isSetupFinished) onNavigateToRoute(MainDestinations.MODE_ROUTE + "?setup=true", true, true)
  }

  val go: (String) -> Unit = { onNavigateToRoute(it, false, true) }
  val notifMode = settings.modeOfOperation == ModeOfOperation.Notification
  val smsMode = settings.modeOfOperation == ModeOfOperation.SMS
  val sensitiveOk =
      checks.sensitive == SensitiveNotifAccess.State.ALLOWED ||
          checks.sensitive == SensitiveNotifAccess.State.NOT_NEEDED
  val sourceOk = (notifMode && checks.notifListener && sensitiveOk) || (smsMode && checks.smsAccess)
  val overall =
      when {
        !checks.loaded -> FdStatus.NEUTRAL
        !notifMode && !smsMode -> FdStatus.ERROR
        !sourceOk -> FdStatus.ERROR
        !checks.unrestricted || !checks.postNotifs -> FdStatus.WARN
        else -> FdStatus.OK
      }

  FdPage(title = stringResource(R.string.otpilot_app_name), subtitle = stringResource(R.string.otp_home_subtitle)) {
    // ── Status ──
    FdCard {
      Row(
          Modifier.fillMaxWidth().padding(FdSpace.l),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(FdSpace.m),
      ) {
        val color = fdStatusColor(overall)
        FdIconBadge(
            if (overall == FdStatus.OK) Icons.Rounded.Flight else Icons.Rounded.WarningAmber,
            tint = color,
            background = color.copy(alpha = 0.14f),
            size = androidx.compose.ui.unit.Dp(48f),
        )
        Column(Modifier.weight(1f)) {
          Text(
              stringResource(
                  when {
                    !notifMode && !smsMode -> R.string.otp_status_setup
                    overall == FdStatus.ERROR -> R.string.otp_status_attention
                    else -> R.string.otp_status_engaged
                  }),
              style = FdType.title,
              color = Fd.colors.textPrimary,
          )
          Text(
              stringResource(if (overall == FdStatus.ERROR) R.string.otp_status_attention_desc else R.string.otp_status_engaged_desc),
              style = FdType.caption,
              color = Fd.colors.textSecondary,
          )
        }
      }
      FdDivider(inset = false)
      FdRow(
          title = stringResource(R.string.otp_mode),
          icon = Icons.Rounded.Tune,
          onClick = { go(MainDestinations.MODE_ROUTE) },
          trailing = {
            FdStatusPill(
                stringResource(
                    when {
                      notifMode -> R.string.otp_mode_notifications
                      smsMode -> R.string.otp_mode_sms
                      else -> R.string.otp_mode_none
                    }),
                if (notifMode || smsMode) FdStatus.NEUTRAL else FdStatus.ERROR,
            )
          },
      )
      if (notifMode) {
        FdDivider()
        FdRow(
            title = stringResource(R.string.otp_notif_access),
            icon = Icons.Rounded.NotificationsActive,
            onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
            trailing = { OkPill(checks.notifListener, R.string.otp_granted, R.string.otp_missing) },
        )
        if (checks.sensitive != SensitiveNotifAccess.State.NOT_NEEDED) {
          FdDivider()
          FdRow(
              title = stringResource(R.string.otp_code_notifs),
              icon = Icons.Rounded.Key,
              onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
              trailing = {
                when (checks.sensitive) {
                  SensitiveNotifAccess.State.ALLOWED -> FdStatusPill(stringResource(R.string.otp_allowed), FdStatus.OK)
                  SensitiveNotifAccess.State.BLOCKED -> FdStatusPill(stringResource(R.string.otp_blocked), FdStatus.ERROR)
                  else -> FdStatusPill(stringResource(R.string.otp_unknown), FdStatus.WARN)
                }
              },
          )
        }
      }
      if (smsMode) {
        FdDivider()
        FdRow(
            title = stringResource(R.string.otp_sms_access),
            icon = Icons.Rounded.Sms,
            onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
            trailing = { OkPill(checks.smsAccess, R.string.otp_granted, R.string.otp_missing) },
        )
      }
      FdDivider()
      FdRow(
          title = stringResource(R.string.otp_background),
          icon = Icons.Rounded.BatteryChargingFull,
          onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
          trailing = { OkPill(checks.unrestricted, R.string.otp_unrestricted, R.string.otp_restricted, warnOnly = true) },
      )
      FdDivider()
      FdRow(
          title = stringResource(R.string.auto_copy),
          icon = Icons.Rounded.ContentCopy,
          onClick = { go(MainDestinations.SETTINGS_ROUTE) },
          trailing = {
            FdStatusPill(
                stringResource(if (settings.isAutoCopyEnabled) R.string.otp_on else R.string.otp_off),
                if (settings.isAutoCopyEnabled) FdStatus.OK else FdStatus.NEUTRAL,
            )
          },
      )
      if (!settings.isAutoCopyEnabled) FdNote(stringResource(R.string.otp_auto_copy_off_hint))
    }

    // ── Last code ──
    if (!settings.isHistoryDisabled) {
      FdSection(stringResource(R.string.otp_last_code), Icons.Rounded.History) {
        val last = if (lastCodes.itemCount > 0) lastCodes.peek(0) else null
        if (last == null) {
          FdNote(stringResource(R.string.otp_last_code_empty))
        } else {
          val code = remember(last.text) { CodeExtractor().getCode(last.text) ?: last.text }
          // "Replace code in history" stores the code as zeros: don't show it as if it were real
          val masked = settings.shouldReplaceCodeInHistory && code.isNotEmpty() && code.all { it == '0' }
          val app = remember(last.packageName) { getAppInfo(context, last.packageName) }
          val copied = stringResource(R.string.code_copied_to_clipboard)
          Column(
              Modifier.fillMaxWidth()
                  .then(
                      Modifier.padding(FdSpace.l)
                  ),
              verticalArrangement = Arrangement.spacedBy(FdSpace.xs),
          ) {
            Text(
                if (masked) "\u2022".repeat(code.length) else code,
                style = FdType.code,
                color = if (masked) Fd.colors.textSecondary else Fd.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                app.appLabel + " · " + DateUtils.getRelativeTimeSpanString(last.createdAt.time),
                style = FdType.caption,
                color = Fd.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
          if (masked) {
            FdDivider(inset = false)
            FdNote(stringResource(R.string.otp_code_masked))
          } else {
          FdDivider(inset = false)
          FdRow(
              title = stringResource(R.string.otp_tap_to_copy),
              icon = Icons.Rounded.ContentCopy,
              showChevron = false,
              onClick = {
                if (Clipboard.copyToClipboard(context, code, settings.isCopyAsNotSensitiveEnabled.not())) {
                  android.widget.Toast.makeText(context, copied, android.widget.Toast.LENGTH_SHORT).show()
                }
              },
          )
          }
        }
      }
    }

    // ── Quick actions ──
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FdSpace.s)) {
      if (notifMode) {
        FdButton(
            stringResource(R.string.otp_send_test),
            onClick = { vm.sendTestNotif(context) },
            icon = Icons.Rounded.Send,
            modifier = Modifier.weight(1f),
        )
      }
      FdButton(
          stringResource(R.string.detection_test),
          onClick = { go(MainDestinations.DETECTION_TEST_ROUTE) },
          icon = Icons.Rounded.ManageSearch,
          style = FdButtonStyle.TONAL,
          modifier = Modifier.weight(1f),
      )
    }

    // ── More ──
    FdSection(stringResource(R.string.otp_more), Icons.Rounded.Apps) {
      if (!settings.isHistoryDisabled) {
        FdRow(stringResource(R.string.history), stringResource(R.string.otp_history_desc), Icons.Rounded.History, onClick = { go(MainDestinations.HISTORY_ROUTE) })
        FdDivider()
      }
      FdRow(stringResource(R.string.ignored_list), stringResource(R.string.otp_ignored_desc), Icons.Rounded.Block, onClick = { go(MainDestinations.IGNORED_APP_LIST_ROUTE) })
      FdDivider()
      FdRow(stringResource(R.string.settings), stringResource(R.string.otp_settings_desc), Icons.Rounded.Settings, onClick = { go(MainDestinations.SETTINGS_ROUTE) })
      FdDivider()
      FdRow(stringResource(R.string.about), stringResource(R.string.otp_about_desc), Icons.Rounded.Info, onClick = { go(MainDestinations.ABOUT_ROUTE) })
    }
  }
}

@Composable
private fun OkPill(ok: Boolean, okText: Int, badText: Int, warnOnly: Boolean = false) {
  FdStatusPill(
      stringResource(if (ok) okText else badText),
      if (ok) FdStatus.OK else if (warnOnly) FdStatus.WARN else FdStatus.ERROR,
  )
}
