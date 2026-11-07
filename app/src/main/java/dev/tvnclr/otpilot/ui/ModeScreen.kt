package dev.tvnclr.otpilot.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jd1378.otphelper.BuildConfig
import io.github.jd1378.otphelper.ModeOfOperation
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.ui.screens.ModeChooseViewModel
import dev.tvnclr.ui.TvButton
import dev.tvnclr.ui.TvCallout
import dev.tvnclr.ui.TvChoiceCard
import dev.tvnclr.ui.TvConfirmDialog
import dev.tvnclr.ui.TvIconButton
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvStatus

@Composable
fun ModeScreen(
    onNavigateToRoute: (String, Boolean, Boolean) -> Unit,
    upPress: () -> Unit,
    vm: ModeChooseViewModel,
    setupMode: Boolean,
) {
  val settings by vm.userSettings.collectAsStateWithLifecycle()
  val showReminder by vm.showPermissionsReminder.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val smsAvailable = BuildConfig.SMS_MODE_AVAILABLE
  val mode = settings.modeOfOperation

  if (showReminder) {
    TvConfirmDialog(
        title = stringResource(R.string.permissions_reminder_title),
        text = stringResource(R.string.permissions_reminder_message),
        confirmText = stringResource(R.string.take_me_there),
        dismissText = stringResource(R.string.okay),
        onConfirm = {
          vm.setShowPermissionsReminder(false)
          onNavigateToRoute(MainDestinations.PERMISSIONS_ROUTE, false, false)
        },
        onDismiss = { vm.setShowPermissionsReminder(false) },
    )
  }

  TvPage(
      title = stringResource(R.string.MODE_ROUTE),
      subtitle = if (setupMode) stringResource(R.string.otp_setup_step, 1, 2) else stringResource(R.string.otp_mode_subtitle),
      onBack = if (setupMode) null else upPress,
      actions = {
        if (setupMode) {
          TvIconButton(Icons.Rounded.Translate, stringResource(R.string.language), {
            onNavigateToRoute(MainDestinations.LANGUAGE_SELECTION_ROUTE, false, true)
          })
        }
      },
      bottomBar =
          if (setupMode) {
            {
              TvButton(
                  stringResource(R.string.next),
                  { onNavigateToRoute(MainDestinations.PERMISSIONS_SETUP_ROUTE, false, false) },
                  Modifier.fillMaxWidth(),
                  enabled = mode != ModeOfOperation.UNRECOGNIZED,
              )
            }
          } else null,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(TvSpace.m)) {
      TvChoiceCard(
          title = stringResource(R.string.Notification),
          description = stringResource(R.string.notification_mode_help),
          icon = Icons.Rounded.Notifications,
          selected = mode == ModeOfOperation.Notification,
          onClick = { vm.onModeSelected(context, ModeOfOperation.Notification) },
      )
      TvChoiceCard(
          title = stringResource(R.string.sms),
          description = stringResource(R.string.sms_mode_help),
          icon = Icons.Rounded.Sms,
          selected = mode == ModeOfOperation.SMS,
          enabled = smsAvailable,
          onClick = { if (smsAvailable) vm.onModeSelected(context, ModeOfOperation.SMS) },
      )
      if (!smsAvailable) TvCallout(stringResource(R.string.mode_sms_disabled_help), TvStatus.NEUTRAL, Icons.Rounded.Info)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      TvSection(stringResource(R.string.otp_mode_android15_title), Icons.Rounded.Info) {
        TvNote(stringResource(R.string.mode_choose_help))
      }
    }
  }
}
