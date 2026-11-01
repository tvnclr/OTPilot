package tvnclr.flightdeck.otpilot.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.ui.components.getAppInfo
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.ui.screens.HistoryDetailViewModel
import io.github.jd1378.otphelper.ui.screens.HistoryViewModel
import io.github.jd1378.otphelper.ui.utils.SkipFirstLaunchedEffect
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdCallout
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdConfirmDialog
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdEmptyState
import tvnclr.flightdeck.ui.FdListPage
import tvnclr.flightdeck.ui.FdMenuItem
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdOverflowMenu
import tvnclr.flightdeck.ui.FdPage
import tvnclr.flightdeck.ui.FdSection
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdStatus
import tvnclr.flightdeck.ui.FdSwitchRow
import tvnclr.flightdeck.ui.FdType

@Composable
fun HistoryScreen(onNavigateToRoute: (String, Boolean, Boolean) -> Unit, upPress: () -> Unit, vm: HistoryViewModel) {
  val items = vm.historyItems.collectAsLazyPagingItems()
  val showClear by vm.showClearHistoryDialog.collectAsStateWithLifecycle()
  val context = LocalContext.current

  if (showClear) {
    FdConfirmDialog(
        title = stringResource(R.string.clear_history),
        text = stringResource(R.string.irreversible_action_warning),
        confirmText = stringResource(R.string.yes),
        dismissText = stringResource(R.string.no),
        onConfirm = { vm.clearHistory() },
        onDismiss = { vm.showClearHistoryDialog.value = false },
        destructive = true,
    )
  }

  FdListPage(
      title = stringResource(R.string.history),
      subtitle = stringResource(R.string.otp_history_subtitle),
      onBack = upPress,
      actions = {
        FdOverflowMenu(
            listOf(FdMenuItem(stringResource(R.string.clear_history), Icons.Outlined.Delete, destructive = true) { vm.showClearHistoryDialog.value = true }),
            stringResource(R.string.options),
        )
      },
  ) {
    when (items.loadState.refresh) {
      is LoadState.Loading -> item { FdLoading() }
      is LoadState.Error -> item { FdCard { FdEmptyState(stringResource(R.string.error), Icons.Rounded.ErrorOutline) } }
      else ->
          if (items.itemCount == 0) {
            item { FdCard { FdEmptyState(stringResource(R.string.history_desc), Icons.Rounded.History) } }
          } else {
            items(items.itemCount, key = items.itemKey { it.id }) { i ->
              val code = items[i] ?: return@items
              val app = remember(code.packageName) { getAppInfo(context, code.packageName) }
              AppCardRow(
                  code.packageName,
                  code.smsOrigin.ifBlank { app.appLabel },
                  relativeTime(code.createdAt),
                  onClick = { onNavigateToRoute(MainDestinations.HISTORY_DETAIL_ROUTE + "/" + code.id, false, true) },
              )
            }
          }
    }
  }
}

@Composable
fun HistoryDetailScreen(upPress: () -> Unit, vm: HistoryDetailViewModel) {
  val state by vm.detectedCode.collectAsStateWithLifecycle()
  val appIgnored by vm.isAppIgnored.collectAsStateWithLifecycle()
  val idIgnored by vm.isNotifIdIgnored.collectAsStateWithLifecycle()
  val tagIgnored by vm.isNotifTagIgnored.collectAsStateWithLifecycle()
  val originIgnored by vm.isSmsOriginIgnored.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val c = Fd.colors

  SkipFirstLaunchedEffect(state) { if (state == null) upPress() }

  val code = state
  FdPage(title = stringResource(R.string.history_detail), onBack = upPress) {
    if (code == null) {
      FdLoading()
      return@FdPage
    }
    val app = remember(code.packageName) { getAppInfo(context, code.packageName) }
    val detection = remember(code.text) { detect(vm.autoUpdatingListenerUtils.codeExtractor, code.text) }

    FdCard {
      Row(
          Modifier.fillMaxWidth().padding(FdSpace.l),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(FdSpace.m),
      ) {
        AppBadge(code.packageName, 56.dp)
        Column(Modifier.weight(1f)) {
          Text(code.smsOrigin.ifBlank { app.appLabel }, style = FdType.title, color = c.textPrimary)
          Text(relativeTime(code.createdAt), style = FdType.caption, color = c.accent)
        }
      }
      if (code.smsOrigin.isBlank() && app.failed) FdNote(stringResource(R.string.app_label_not_visible_reason))
    }

    FdSection(stringResource(R.string.otp_detail_message), Icons.Rounded.Message) {
      Column(Modifier.fillMaxWidth().padding(FdSpace.l), verticalArrangement = Arrangement.spacedBy(FdSpace.m)) {
        SelectionContainer { Text(highlighted(code.text, detection), style = FdType.body, color = c.textPrimary) }
        if (detection.codeRange != null) {
          Text(stringResource(R.string.otp_detail_legend), style = FdType.caption, color = c.textSecondary)
        }
      }
    }

    FdSection(stringResource(R.string.otp_detail_ignore), Icons.Rounded.Block) {
      if (code.smsOrigin.isBlank()) {
        FdSwitchRow(stringResource(R.string.ignore_app), appIgnored, { vm.toggleAppIgnore(code) }, subtitle = app.appLabel, icon = Icons.Rounded.Apps)
      } else {
        FdSwitchRow(stringResource(R.string.ignore_origin), originIgnored, { vm.toggleSmsOriginIgnore(code) },
            subtitle = stringResource(R.string.sms_origin) + ": " + code.smsOrigin, icon = Icons.Rounded.Person)
      }
      if (code.notificationId.isNotBlank()) {
        FdDivider()
        FdSwitchRow(stringResource(R.string.ignore_id), idIgnored, { vm.toggleNotifIdIgnore(code) },
            subtitle = stringResource(R.string.notification_id) + ": " + code.notificationId, icon = Icons.Rounded.Numbers)
      }
      if (code.notificationTag.isNotBlank()) {
        FdDivider()
        FdSwitchRow(stringResource(R.string.ignore_tag), tagIgnored, { vm.toggleNotifTagIgnore(code) },
            subtitle = stringResource(R.string.notification_tag) + ": " + code.notificationTag, icon = Icons.Rounded.Sell)
      }
      FdNote(stringResource(R.string.otp_detail_ignore_desc))
    }
  }
}
