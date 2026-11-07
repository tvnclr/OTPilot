package dev.tvnclr.otpilot.ui

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
import dev.tvnclr.ui.Tv
import dev.tvnclr.ui.TvCallout
import dev.tvnclr.ui.TvCard
import dev.tvnclr.ui.TvConfirmDialog
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvEmptyState
import dev.tvnclr.ui.TvListPage
import dev.tvnclr.ui.TvMenuItem
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvOverflowMenu
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvStatus
import dev.tvnclr.ui.TvSwitchRow
import dev.tvnclr.ui.TvType

@Composable
fun HistoryScreen(onNavigateToRoute: (String, Boolean, Boolean) -> Unit, upPress: () -> Unit, vm: HistoryViewModel) {
  val items = vm.historyItems.collectAsLazyPagingItems()
  val showClear by vm.showClearHistoryDialog.collectAsStateWithLifecycle()
  val context = LocalContext.current

  if (showClear) {
    TvConfirmDialog(
        title = stringResource(R.string.clear_history),
        text = stringResource(R.string.irreversible_action_warning),
        confirmText = stringResource(R.string.yes),
        dismissText = stringResource(R.string.no),
        onConfirm = { vm.clearHistory() },
        onDismiss = { vm.showClearHistoryDialog.value = false },
        destructive = true,
    )
  }

  TvListPage(
      title = stringResource(R.string.history),
      subtitle = stringResource(R.string.otp_history_subtitle),
      onBack = upPress,
      actions = {
        TvOverflowMenu(
            listOf(TvMenuItem(stringResource(R.string.clear_history), Icons.Outlined.Delete, destructive = true) { vm.showClearHistoryDialog.value = true }),
            stringResource(R.string.options),
        )
      },
  ) {
    when (items.loadState.refresh) {
      is LoadState.Loading -> item { TvLoading() }
      is LoadState.Error -> item { TvCard { TvEmptyState(stringResource(R.string.error), Icons.Rounded.ErrorOutline) } }
      else ->
          if (items.itemCount == 0) {
            item { TvCard { TvEmptyState(stringResource(R.string.history_desc), Icons.Rounded.History) } }
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
  val c = Tv.colors

  SkipFirstLaunchedEffect(state) { if (state == null) upPress() }

  val code = state
  TvPage(title = stringResource(R.string.history_detail), onBack = upPress) {
    if (code == null) {
      TvLoading()
      return@TvPage
    }
    val app = remember(code.packageName) { getAppInfo(context, code.packageName) }
    val detection = remember(code.text) { detect(vm.autoUpdatingListenerUtils.codeExtractor, code.text) }

    TvCard {
      Row(
          Modifier.fillMaxWidth().padding(TvSpace.l),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(TvSpace.m),
      ) {
        AppBadge(code.packageName, 56.dp)
        Column(Modifier.weight(1f)) {
          Text(code.smsOrigin.ifBlank { app.appLabel }, style = TvType.title, color = c.textPrimary)
          Text(relativeTime(code.createdAt), style = TvType.caption, color = c.accent)
        }
      }
      if (code.smsOrigin.isBlank() && app.failed) TvNote(stringResource(R.string.app_label_not_visible_reason))
    }

    TvSection(stringResource(R.string.otp_detail_message), Icons.Rounded.Message) {
      Column(Modifier.fillMaxWidth().padding(TvSpace.l), verticalArrangement = Arrangement.spacedBy(TvSpace.m)) {
        SelectionContainer { Text(highlighted(code.text, detection), style = TvType.body, color = c.textPrimary) }
        if (detection.codeRange != null) {
          Text(stringResource(R.string.otp_detail_legend), style = TvType.caption, color = c.textSecondary)
        }
      }
    }

    TvSection(stringResource(R.string.otp_detail_ignore), Icons.Rounded.Block) {
      if (code.smsOrigin.isBlank()) {
        TvSwitchRow(stringResource(R.string.ignore_app), appIgnored, { vm.toggleAppIgnore(code) }, subtitle = app.appLabel, icon = Icons.Rounded.Apps)
      } else {
        TvSwitchRow(stringResource(R.string.ignore_origin), originIgnored, { vm.toggleSmsOriginIgnore(code) },
            subtitle = stringResource(R.string.sms_origin) + ": " + code.smsOrigin, icon = Icons.Rounded.Person)
      }
      if (code.notificationId.isNotBlank()) {
        TvDivider()
        TvSwitchRow(stringResource(R.string.ignore_id), idIgnored, { vm.toggleNotifIdIgnore(code) },
            subtitle = stringResource(R.string.notification_id) + ": " + code.notificationId, icon = Icons.Rounded.Numbers)
      }
      if (code.notificationTag.isNotBlank()) {
        TvDivider()
        TvSwitchRow(stringResource(R.string.ignore_tag), tagIgnored, { vm.toggleNotifTagIgnore(code) },
            subtitle = stringResource(R.string.notification_tag) + ": " + code.notificationTag, icon = Icons.Rounded.Sell)
      }
      TvNote(stringResource(R.string.otp_detail_ignore_desc))
    }
  }
}
