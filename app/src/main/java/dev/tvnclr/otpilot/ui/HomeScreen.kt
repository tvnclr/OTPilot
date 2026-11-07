package dev.tvnclr.otpilot.ui

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
import dev.tvnclr.otpilot.SensitiveNotifAccess
import dev.tvnclr.ui.Fd
import dev.tvnclr.ui.TvButton
import dev.tvnclr.ui.TvButtonStyle
import dev.tvnclr.ui.TvCard
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvIconBadge
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvRow
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvStatus
import dev.tvnclr.ui.TvStatusPill
import dev.tvnclr.ui.TvType
import dev.tvnclr.ui.tvStatusColor

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
        !checks.loaded -> TvStatus.NEUTRAL
        !notifMode && !smsMode -> TvStatus.ERROR
        !sourceOk -> TvStatus.ERROR
        !checks.unrestricted || !checks.postNotifs -> TvStatus.WARN
        else -> TvStatus.OK
      }

  TvPage(title = stringResource(R.string.otpilot_app_name), subtitle = stringResource(R.string.otp_home_subtitle), hero = true) {
    // ── Status ──
    TvCard {
      Row(
          Modifier.fillMaxWidth().padding(TvSpace.l),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(TvSpace.m),
      ) {
        val color = tvStatusColor(overall)
        TvIconBadge(
            if (overall == TvStatus.OK) Icons.Rounded.Flight else Icons.Rounded.WarningAmber,
            tint = color,
            background = color.copy(alpha = 0.14f),
            size = androidx.compose.ui.unit.Dp(48f),
        )
        Column(Modifier.weight(1f)) {
          Text(
              stringResource(
                  when {
                    !notifMode && !smsMode -> R.string.otp_status_setup
                    overall == TvStatus.ERROR -> R.string.otp_status_attention
                    else -> R.string.otp_status_engaged
                  }),
              style = TvType.title,
              color = Tv.colors.textPrimary,
          )
          Text(
              stringResource(if (overall == TvStatus.ERROR) R.string.otp_status_attention_desc else R.string.otp_status_engaged_desc),
              style = TvType.caption,
              color = Tv.colors.textSecondary,
          )
        }
      }
      TvDivider(inset = false)
      TvRow(
          title = stringResource(R.string.otp_mode),
          icon = Icons.Rounded.Tune,
          onClick = { go(MainDestinations.MODE_ROUTE) },
          trailing = {
            TvStatusPill(
                stringResource(
                    when {
                      notifMode -> R.string.otp_mode_notifications
                      smsMode -> R.string.otp_mode_sms
                      else -> R.string.otp_mode_none
                    }),
                if (notifMode || smsMode) TvStatus.NEUTRAL else TvStatus.ERROR,
            )
          },
      )
      if (notifMode) {
        TvDivider()
        TvRow(
            title = stringResource(R.string.otp_notif_access),
            icon = Icons.Rounded.NotificationsActive,
            onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
            trailing = { OkPill(checks.notifListener, R.string.otp_granted, R.string.otp_missing) },
        )
        if (checks.sensitive != SensitiveNotifAccess.State.NOT_NEEDED) {
          TvDivider()
          TvRow(
              title = stringResource(R.string.otp_code_notifs),
              icon = Icons.Rounded.Key,
              onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
              trailing = {
                when (checks.sensitive) {
                  SensitiveNotifAccess.State.ALLOWED -> TvStatusPill(stringResource(R.string.otp_allowed), TvStatus.OK)
                  SensitiveNotifAccess.State.BLOCKED -> TvStatusPill(stringResource(R.string.otp_blocked), TvStatus.ERROR)
                  else -> TvStatusPill(stringResource(R.string.otp_unknown), TvStatus.WARN)
                }
              },
          )
        }
      }
      if (smsMode) {
        TvDivider()
        TvRow(
            title = stringResource(R.string.otp_sms_access),
            icon = Icons.Rounded.Sms,
            onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
            trailing = { OkPill(checks.smsAccess, R.string.otp_granted, R.string.otp_missing) },
        )
      }
      TvDivider()
      TvRow(
          title = stringResource(R.string.otp_background),
          icon = Icons.Rounded.BatteryChargingFull,
          onClick = { go(MainDestinations.PERMISSIONS_ROUTE) },
          trailing = { OkPill(checks.unrestricted, R.string.otp_unrestricted, R.string.otp_restricted, warnOnly = true) },
      )
      TvDivider()
      TvRow(
          title = stringResource(R.string.auto_copy),
          icon = Icons.Rounded.ContentCopy,
          onClick = { go(MainDestinations.SETTINGS_ROUTE) },
          trailing = {
            TvStatusPill(
                stringResource(if (settings.isAutoCopyEnabled) R.string.otp_on else R.string.otp_off),
                if (settings.isAutoCopyEnabled) TvStatus.OK else TvStatus.NEUTRAL,
            )
          },
      )
      if (!settings.isAutoCopyEnabled) TvNote(stringResource(R.string.otp_auto_copy_off_hint))
    }

    // ── Last code ──
    if (!settings.isHistoryDisabled) {
      TvSection(stringResource(R.string.otp_last_code), Icons.Rounded.History) {
        val last = if (lastCodes.itemCount > 0) lastCodes.peek(0) else null
        if (last == null) {
          TvNote(stringResource(R.string.otp_last_code_empty))
        } else {
          val code = remember(last.text) { CodeExtractor().getCode(last.text) ?: last.text }
          // "Replace code in history" stores the code as zeros: don't show it as if it were real
          val masked = settings.shouldReplaceCodeInHistory && code.isNotEmpty() && code.all { it == '0' }
          val app = remember(last.packageName) { getAppInfo(context, last.packageName) }
          val copied = stringResource(R.string.code_copied_to_clipboard)
          Column(
              Modifier.fillMaxWidth()
                  .then(
                      Modifier.padding(TvSpace.l)
                  ),
              verticalArrangement = Arrangement.spacedBy(TvSpace.xs),
          ) {
            Text(
                if (masked) "\u2022".repeat(code.length) else code,
                style = TvType.code,
                color = if (masked) Tv.colors.textSecondary else Tv.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                app.appLabel + " · " + DateUtils.getRelativeTimeSpanString(last.createdAt.time),
                style = TvType.caption,
                color = Tv.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
          if (masked) {
            TvDivider(inset = false)
            TvNote(stringResource(R.string.otp_code_masked))
          } else {
          TvDivider(inset = false)
          TvRow(
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
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TvSpace.s)) {
      if (notifMode) {
        TvButton(
            stringResource(R.string.otp_send_test),
            onClick = { vm.sendTestNotif(context) },
            icon = Icons.Rounded.Send,
            modifier = Modifier.weight(1f),
        )
      }
      TvButton(
          stringResource(R.string.detection_test),
          onClick = { go(MainDestinations.DETECTION_TEST_ROUTE) },
          icon = Icons.Rounded.ManageSearch,
          style = TvButtonStyle.TONAL,
          modifier = Modifier.weight(1f),
      )
    }

    // ── More ──
    TvSection(stringResource(R.string.otp_more), Icons.Rounded.Apps) {
      if (!settings.isHistoryDisabled) {
        TvRow(stringResource(R.string.history), stringResource(R.string.otp_history_desc), Icons.Rounded.History, onClick = { go(MainDestinations.HISTORY_ROUTE) })
        TvDivider()
      }
      TvRow(stringResource(R.string.ignored_list), stringResource(R.string.otp_ignored_desc), Icons.Rounded.Block, onClick = { go(MainDestinations.IGNORED_APP_LIST_ROUTE) })
      TvDivider()
      TvRow(stringResource(R.string.settings), stringResource(R.string.otp_settings_desc), Icons.Rounded.Settings, onClick = { go(MainDestinations.SETTINGS_ROUTE) })
      TvDivider()
      TvRow(stringResource(R.string.about), stringResource(R.string.otp_about_desc), Icons.Rounded.Info, onClick = { go(MainDestinations.ABOUT_ROUTE) })
    }
  }
}

@Composable
private fun OkPill(ok: Boolean, okText: Int, badText: Int, warnOnly: Boolean = false) {
  TvStatusPill(
      stringResource(if (ok) okText else badText),
      if (ok) TvStatus.OK else if (warnOnly) TvStatus.WARN else TvStatus.ERROR,
  )
}
