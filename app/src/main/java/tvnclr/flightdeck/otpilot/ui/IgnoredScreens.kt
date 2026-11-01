package tvnclr.flightdeck.otpilot.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ErrorOutline
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
import io.github.jd1378.otphelper.data.local.entity.IgnoredNotifType
import io.github.jd1378.otphelper.model.getTranslation
import io.github.jd1378.otphelper.ui.components.getAppInfo
import io.github.jd1378.otphelper.ui.components.shortenAppLabel
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.ui.screens.IgnoredAppDetailViewModel
import io.github.jd1378.otphelper.ui.screens.IgnoredAppListViewModel
import io.github.jd1378.otphelper.ui.utils.SkipFirstLaunchedEffect
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdEmptyState
import tvnclr.flightdeck.ui.FdListPage
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdStatus
import tvnclr.flightdeck.ui.FdType
import tvnclr.flightdeck.ui.fdStatusColor

@Composable
fun IgnoredListScreen(onNavigateToRoute: (String, Boolean, Boolean) -> Unit, upPress: () -> Unit, vm: IgnoredAppListViewModel) {
  val items = vm.ignoredApps.collectAsLazyPagingItems()
  val context = LocalContext.current
  FdListPage(
      title = stringResource(R.string.IGNORED_LIST_ROUTE),
      subtitle = stringResource(R.string.otp_ignored_subtitle),
      onBack = upPress,
  ) {
    when (items.loadState.refresh) {
      is LoadState.Loading -> item { FdLoading() }
      is LoadState.Error -> item { FdCard { FdEmptyState(stringResource(R.string.error), Icons.Rounded.ErrorOutline) } }
      else ->
          if (items.itemCount == 0) {
            item { FdCard { FdEmptyState(stringResource(R.string.ignored_list_description), Icons.Rounded.Block) } }
          } else {
            items(items.itemCount, key = items.itemKey { it.packageName }) { i ->
              val group = items[i] ?: return@items
              val app = remember(group.packageName) { getAppInfo(context, group.packageName) }
              AppCardRow(
                  group.packageName,
                  app.shortenAppLabel(40),
                  stringResource(R.string.n_items, group.totalItems),
                  onClick = { onNavigateToRoute(MainDestinations.IGNORED_APP_DETAIL_ROUTE + "/" + group.packageName, false, true) },
              )
            }
            if (items.loadState.append == LoadState.Loading) item { FdLoading() }
          }
    }
  }
}

@Composable
fun IgnoredDetailScreen(upPress: () -> Unit, vm: IgnoredAppDetailViewModel) {
  val items = vm.ignoredItems.collectAsLazyPagingItems()
  val pkg by vm.packageName.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val app = remember(pkg) { getAppInfo(context, pkg) }
  val c = Fd.colors

  SkipFirstLaunchedEffect(items.itemCount) { if (items.itemCount == 0) upPress() }

  FdListPage(title = app.shortenAppLabel(30), subtitle = pkg, onBack = upPress) {
    item(key = "header") {
      FdCard {
        Row(
            Modifier.fillMaxWidth().padding(FdSpace.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FdSpace.m),
        ) {
          AppBadge(pkg, 56.dp)
          Column(Modifier.weight(1f)) {
            Text(app.appLabel, style = FdType.title, color = c.textPrimary)
            Text(stringResource(R.string.n_items, items.itemCount.toLong()), style = FdType.caption, color = c.accent)
          }
        }
        if (app.failed) FdNote(stringResource(R.string.app_label_not_visible_reason))
      }
    }
    if (items.loadState.refresh is LoadState.Error) {
      item { FdCard { FdEmptyState(stringResource(R.string.error), Icons.Rounded.ErrorOutline) } }
    } else {
      items(items.itemCount, key = items.itemKey { it.id }) { i ->
        val n = items[i] ?: return@items
        FdCard(Modifier.animateItem(fadeOutSpec = null)) {
          val icon =
              when (n.type) {
                IgnoredNotifType.APPLICATION -> Icons.Rounded.Apps
                IgnoredNotifType.NOTIFICATION_ID -> Icons.Rounded.Numbers
                IgnoredNotifType.NOTIFICATION_TAG -> Icons.Rounded.Sell
                IgnoredNotifType.SMS_ORIGIN -> Icons.Rounded.Person
                else -> Icons.Rounded.Block
              }
          FdRow(
              title = if (n.type == IgnoredNotifType.APPLICATION) stringResource(R.string.ignore_app) else n.type.getTranslation(),
              subtitle = if (n.type == IgnoredNotifType.APPLICATION) app.appLabel else n.typeData,
              icon = icon,
              trailing = { RowAction(stringResource(R.string.otp_ignored_remove), { vm.removeIgnoredNotif(n) }, color = fdStatusColor(FdStatus.ERROR)) },
          )
        }
      }
    }
  }
}
