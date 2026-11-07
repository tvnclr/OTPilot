package dev.tvnclr.otpilot.ui

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
import dev.tvnclr.ui.Tv
import dev.tvnclr.ui.TvCard
import dev.tvnclr.ui.TvEmptyState
import dev.tvnclr.ui.TvListPage
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvRow
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvStatus
import dev.tvnclr.ui.TvType
import dev.tvnclr.ui.tvStatusColor

@Composable
fun IgnoredListScreen(onNavigateToRoute: (String, Boolean, Boolean) -> Unit, upPress: () -> Unit, vm: IgnoredAppListViewModel) {
  val items = vm.ignoredApps.collectAsLazyPagingItems()
  val context = LocalContext.current
  TvListPage(
      title = stringResource(R.string.IGNORED_LIST_ROUTE),
      subtitle = stringResource(R.string.otp_ignored_subtitle),
      onBack = upPress,
  ) {
    when (items.loadState.refresh) {
      is LoadState.Loading -> item { TvLoading() }
      is LoadState.Error -> item { TvCard { TvEmptyState(stringResource(R.string.error), Icons.Rounded.ErrorOutline) } }
      else ->
          if (items.itemCount == 0) {
            item { TvCard { TvEmptyState(stringResource(R.string.ignored_list_description), Icons.Rounded.Block) } }
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
            if (items.loadState.append == LoadState.Loading) item { TvLoading() }
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
  val c = Tv.colors

  SkipFirstLaunchedEffect(items.itemCount) { if (items.itemCount == 0) upPress() }

  TvListPage(title = app.shortenAppLabel(30), subtitle = pkg, onBack = upPress) {
    item(key = "header") {
      TvCard {
        Row(
            Modifier.fillMaxWidth().padding(TvSpace.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TvSpace.m),
        ) {
          AppBadge(pkg, 56.dp)
          Column(Modifier.weight(1f)) {
            Text(app.appLabel, style = TvType.title, color = c.textPrimary)
            Text(stringResource(R.string.n_items, items.itemCount.toLong()), style = TvType.caption, color = c.accent)
          }
        }
        if (app.failed) TvNote(stringResource(R.string.app_label_not_visible_reason))
      }
    }
    if (items.loadState.refresh is LoadState.Error) {
      item { TvCard { TvEmptyState(stringResource(R.string.error), Icons.Rounded.ErrorOutline) } }
    } else {
      items(items.itemCount, key = items.itemKey { it.id }) { i ->
        val n = items[i] ?: return@items
        TvCard(Modifier.animateItem(fadeOutSpec = null)) {
          val icon =
              when (n.type) {
                IgnoredNotifType.APPLICATION -> Icons.Rounded.Apps
                IgnoredNotifType.NOTIFICATION_ID -> Icons.Rounded.Numbers
                IgnoredNotifType.NOTIFICATION_TAG -> Icons.Rounded.Sell
                IgnoredNotifType.SMS_ORIGIN -> Icons.Rounded.Person
                else -> Icons.Rounded.Block
              }
          TvRow(
              title = if (n.type == IgnoredNotifType.APPLICATION) stringResource(R.string.ignore_app) else n.type.getTranslation(),
              subtitle = if (n.type == IgnoredNotifType.APPLICATION) app.appLabel else n.typeData,
              icon = icon,
              trailing = { RowAction(stringResource(R.string.otp_ignored_remove), { vm.removeIgnoredNotif(n) }, color = tvStatusColor(TvStatus.ERROR)) },
          )
        }
      }
    }
  }
}
