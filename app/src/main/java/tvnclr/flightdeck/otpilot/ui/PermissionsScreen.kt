package tvnclr.flightdeck.otpilot.ui

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
import tvnclr.flightdeck.otpilot.SensitiveNotifAccess
import androidx.compose.material3.Text
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdButton
import tvnclr.flightdeck.ui.FdType
import tvnclr.flightdeck.ui.FdButtonStyle
import tvnclr.flightdeck.ui.FdCallout
import tvnclr.flightdeck.ui.FdCodeBlock
import tvnclr.flightdeck.ui.FdConfirmDialog
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdPage
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSection
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdStatus
import tvnclr.flightdeck.ui.FdStatusPill

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
    FdConfirmDialog(
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

  FdPage(
      title = stringResource(R.string.PERMISSION_ROUTE),
      subtitle = if (setupMode) stringResource(R.string.otp_setup_step, 2, 2) else stringResource(R.string.otp_perm_subtitle),
      onBack = upPress,
      bottomBar =
          if (setupMode) {
            {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FdSpace.m)) {
                if (ui.hasDoneAllSteps) {
                  FdButton(stringResource(R.string.finish), { vm.onSetupFinish(onNavigateToRoute) }, Modifier.weight(1f))
                } else {
                  FdButton(stringResource(R.string.skip), { vm.onSetupSkipPressed() }, Modifier.weight(1f), style = FdButtonStyle.OUTLINE)
                }
              }
            }
          } else null,
  ) {
    Text(stringResource(R.string.permissions_desc), style = FdType.body, color = Fd.colors.textSecondary)

    FdSection(stringResource(R.string.otp_perm_required), Icons.Rounded.VerifiedUser) {
      PermRow(
          stringResource(R.string.permission_todo_post_notifications),
          Icons.Rounded.Notifications,
          ui.hasNotifPerm,
          stringResource(R.string.request),
      ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      if (smsMode) {
        FdDivider()
        PermRow(stringResource(R.string.permission_todo_receive_sms), Icons.Rounded.Sms, ui.hasSmsListenerPerm, stringResource(R.string.request)) {
          launcher.launch(Manifest.permission.RECEIVE_SMS)
        }
        FdDivider()
        PermRow(stringResource(R.string.permission_todo_read_sms), Icons.Rounded.MarkEmailRead, ui.hasReadSmsPerm, stringResource(R.string.request)) {
          launcher.launch(Manifest.permission.READ_SMS)
        }
      }
      FdDivider()
      PermRow(
          stringResource(R.string.permission_todo_read_notifications),
          Icons.Rounded.NotificationsActive,
          ui.hasNotifListenerPerm,
          stringResource(R.string.open),
      ) {
        vm.onOpenReadNotificationsPressed(context)
      }
      if (smsMode) FdNote(stringResource(R.string.read_notifs_sms_mode_desc))
    }

    if (notifMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      val allowed = sensitive == SensitiveNotifAccess.State.ALLOWED
      FdSection(stringResource(R.string.otp_perm_code_notifs), Icons.Rounded.Key) {
        PermRow(stringResource(R.string.otpilot_sensitive_todo), Icons.Rounded.Lock, allowed, stringResource(R.string.otpilot_sensitive_grant)) {
          vm.onGrantSensitiveNotifsPressed(context)
        }
        FdNote(
            stringResource(
                when (sensitive) {
                  SensitiveNotifAccess.State.ALLOWED -> R.string.otpilot_sensitive_desc_allowed
                  SensitiveNotifAccess.State.UNKNOWN -> R.string.otpilot_sensitive_desc_unknown
                  else -> R.string.otpilot_sensitive_desc_blocked
                }))
        if (!allowed) {
          Column(
              Modifier.fillMaxWidth().padding(start = FdSpace.l, end = FdSpace.l, bottom = FdSpace.l),
              verticalArrangement = Arrangement.spacedBy(FdSpace.s),
          ) {
            if (sensitiveFailed) FdCallout(stringResource(R.string.otpilot_sensitive_grant_failed))
            Text(stringResource(R.string.otp_perm_manual), style = FdType.caption, color = Fd.colors.textSecondary)
            FdCodeBlock(stringResource(R.string.adb_command_sensitive_notifs))
            FdCodeBlock(stringResource(R.string.adb_command_kill_app))
          }
        }
      }
    }

    FdSection(stringResource(R.string.otp_perm_background), Icons.Rounded.BatteryChargingFull) {
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
      FdSection(stringResource(R.string.otp_perm_device), Icons.Rounded.AppSettingsAlt) {
        if (ui.hasAutostartSettings) {
          FdNote(stringResource(R.string.perm_extra_desc))
          FdRow(
              stringResource(R.string.permission_todo_autostart),
              icon = Icons.Rounded.PowerSettingsNew,
              onClick = { vm.onOpenAutostartPressed(context) },
          )
        }
        if (ui.hasAutostartSettings && ui.hasRestrictedSettings) FdDivider(inset = false)
        if (ui.hasRestrictedSettings) {
          FdNote(stringResource(R.string.perm_restricted_desc))
          FdRow(
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
    FdRow(
        title,
        icon = icon,
        onClick = if (granted) null else onAction,
        showChevron = false,
        trailing = {
          if (granted) FdStatusPill(stringResource(R.string.otp_granted), FdStatus.OK) else RowAction(action, onAction)
        },
    )
  }
}
