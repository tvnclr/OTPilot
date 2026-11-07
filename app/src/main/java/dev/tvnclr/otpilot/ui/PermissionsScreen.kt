package dev.tvnclr.otpilot.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AppSettingsAlt
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jd1378.otphelper.ModeOfOperation
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.screens.PermissionsViewModel
import dev.tvnclr.otpilot.SensitiveNotifAccess
import androidx.compose.material3.Text
import dev.tvnclr.ui.Fd
import dev.tvnclr.ui.TvButton
import dev.tvnclr.ui.TvType
import dev.tvnclr.ui.TvButtonStyle
import dev.tvnclr.ui.TvCallout
import dev.tvnclr.ui.TvCodeBlock
import dev.tvnclr.ui.TvConfirmDialog
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvRow
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvStatus
import dev.tvnclr.ui.TvStatusPill

@Composable
fun PermissionsScreen(
    onNavigateToRoute: (String, Boolean, Boolean) -> Unit,
    upPress: () -> Unit,
    vm: PermissionsViewModel,
    setupMode: Boolean,
) {
  val ui by vm.uiState.collectAsStateWithLifecycle()
  val sensitive by vm.sensitiveNotifState.collectAsStateWithLifecycle()
  val sensitiveFailed by vm.sensitiveGrantFailed.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  val lifecycleState by lifecycle.currentStateFlow.collectAsStateWithLifecycle()
  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.updatePermissionsStatus(context) }

  LaunchedEffect(lifecycleState) {
    if (lifecycleState == Lifecycle.State.STARTED || lifecycleState == Lifecycle.State.RESUMED) vm.updatePermissionsStatus(context)
  }

  if (ui.showSkipWarning) {
    TvConfirmDialog(
        title = stringResource(R.string.notice),
        text = stringResource(R.string.skip_warning),
        confirmText = stringResource(R.string.okay),
        dismissText = null,
        onConfirm = { vm.onSetupFinish(onNavigateToRoute) },
        onDismiss = {}, // upstream: the warning can only be acknowledged
    )
  }

  val notifMode = ui.modeOfOperation == ModeOfOperation.Notification
  val smsMode = ui.modeOfOperation == ModeOfOperation.SMS

  TvPage(
      title = stringResource(R.string.PERMISSION_ROUTE),
      subtitle = if (setupMode) stringResource(R.string.otp_setup_step, 2, 2) else stringResource(R.string.otp_perm_subtitle),
      onBack = upPress,
      bottomBar =
          if (setupMode) {
            {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TvSpace.m)) {
                if (ui.hasDoneAllSteps) {
                  TvButton(stringResource(R.string.finish), { vm.onSetupFinish(onNavigateToRoute) }, Modifier.weight(1f))
                } else {
                  TvButton(stringResource(R.string.skip), { vm.onSetupSkipPressed() }, Modifier.weight(1f), style = TvButtonStyle.OUTLINE)
                }
              }
            }
          } else null,
  ) {
    Text(stringResource(R.string.permissions_desc), style = TvType.body, color = Tv.colors.textSecondary, modifier = Modifier.padding(start = TvSpace.xs))

    TvSection(stringResource(R.string.otp_perm_required), Icons.Rounded.VerifiedUser) {
      PermRow(
          stringResource(R.string.permission_todo_post_notifications),
          Icons.Rounded.Notifications,
          ui.hasNotifPerm,
          stringResource(R.string.request),
      ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      if (smsMode) {
        TvDivider()
        PermRow(stringResource(R.string.permission_todo_receive_sms), Icons.Rounded.Sms, ui.hasSmsListenerPerm, stringResource(R.string.request)) {
          launcher.launch(Manifest.permission.RECEIVE_SMS)
        }
        TvDivider()
        PermRow(stringResource(R.string.permission_todo_read_sms), Icons.Rounded.MarkEmailRead, ui.hasReadSmsPerm, stringResource(R.string.request)) {
          launcher.launch(Manifest.permission.READ_SMS)
        }
      }
      TvDivider()
      PermRow(
          stringResource(R.string.permission_todo_read_notifications),
          Icons.Rounded.NotificationsActive,
          ui.hasNotifListenerPerm,
          stringResource(R.string.open),
      ) {
        vm.onOpenReadNotificationsPressed(context)
      }
      if (smsMode) TvNote(stringResource(R.string.read_notifs_sms_mode_desc))
    }

    if (notifMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      val allowed = sensitive == SensitiveNotifAccess.State.ALLOWED
      TvSection(stringResource(R.string.otp_perm_code_notifs), Icons.Rounded.Key) {
        PermRow(stringResource(R.string.otpilot_sensitive_todo), Icons.Rounded.Lock, allowed, stringResource(R.string.otpilot_sensitive_grant)) {
          vm.onGrantSensitiveNotifsPressed(context)
        }
        TvNote(
            stringResource(
                when (sensitive) {
                  SensitiveNotifAccess.State.ALLOWED -> R.string.otpilot_sensitive_desc_allowed
                  SensitiveNotifAccess.State.UNKNOWN -> R.string.otpilot_sensitive_desc_unknown
                  else -> R.string.otpilot_sensitive_desc_blocked
                }))
        if (!allowed) {
          Column(
              Modifier.fillMaxWidth().padding(start = TvSpace.l, end = TvSpace.l, bottom = TvSpace.l),
              verticalArrangement = Arrangement.spacedBy(TvSpace.s),
          ) {
            if (sensitiveFailed) TvCallout(stringResource(R.string.otpilot_sensitive_grant_failed))
            Text(stringResource(R.string.otp_perm_manual), style = TvType.caption, color = Tv.colors.textSecondary)
            TvCodeBlock(stringResource(R.string.adb_command_sensitive_notifs))
            TvCodeBlock(stringResource(R.string.adb_command_kill_app))
          }
        }
      }
    }

    TvSection(stringResource(R.string.otp_perm_background), Icons.Rounded.BatteryChargingFull) {
      PermRow(
          stringResource(R.string.permission_todo_remain_open),
          Icons.Rounded.BatteryChargingFull,
          ui.isIgnoringBatteryOptimizations,
          stringResource(R.string.open),
      ) {
        vm.onOpenBatteryOptimizationsPressed(context)
      }
    }

    if (ui.hasAutostartSettings || ui.hasRestrictedSettings) {
      TvSection(stringResource(R.string.otp_perm_device), Icons.Rounded.AppSettingsAlt) {
        if (ui.hasAutostartSettings) {
          TvNote(stringResource(R.string.perm_extra_desc))
          TvRow(
              stringResource(R.string.permission_todo_autostart),
              icon = Icons.Rounded.PowerSettingsNew,
              onClick = { vm.onOpenAutostartPressed(context) },
          )
        }
        if (ui.hasAutostartSettings && ui.hasRestrictedSettings) TvDivider(inset = false)
        if (ui.hasRestrictedSettings) {
          TvNote(stringResource(R.string.perm_restricted_desc))
          TvRow(
              stringResource(R.string.permission_todo_allow_restricted_settings),
              icon = Icons.Rounded.AppSettingsAlt,
              onClick = { vm.onOpenAppSettings(context) },
          )
        }
      }
    }
  }
}

@Composable
private fun PermRow(title: String, icon: ImageVector, granted: Boolean, action: String, onAction: () -> Unit) {
  val grantedLabel = stringResource(R.string.permission_granted_to)
  val missingLabel = stringResource(R.string.permission_not_granted_to)
  Column(Modifier.semantics { stateDescription = if (granted) grantedLabel else missingLabel }) {
    TvRow(
        title,
        icon = icon,
        onClick = if (granted) null else onAction,
        showChevron = false,
        trailing = {
          if (granted) TvStatusPill(stringResource(R.string.otp_granted), TvStatus.OK) else RowAction(action, onAction)
        },
    )
  }
}
