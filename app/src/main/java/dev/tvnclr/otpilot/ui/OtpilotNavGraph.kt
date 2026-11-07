package dev.tvnclr.otpilot.ui

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import io.github.jd1378.otphelper.OTPHELPER_APP_SCHEME
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.ui.navigation.NavArgs
import io.github.jd1378.otphelper.ui.screens.CleanupPhrasesViewModel
import io.github.jd1378.otphelper.ui.screens.DetectionTestViewModel
import io.github.jd1378.otphelper.ui.screens.HistoryDetailViewModel
import io.github.jd1378.otphelper.ui.screens.HistoryViewModel
import io.github.jd1378.otphelper.ui.screens.IgnoredAppDetailViewModel
import io.github.jd1378.otphelper.ui.screens.IgnoredAppListViewModel
import io.github.jd1378.otphelper.ui.screens.IgnoredPhrasesViewModel
import io.github.jd1378.otphelper.ui.screens.LanguageSelectionViewModel
import io.github.jd1378.otphelper.ui.screens.ModeChooseViewModel
import io.github.jd1378.otphelper.ui.screens.PermissionsViewModel
import io.github.jd1378.otphelper.ui.screens.SensitivePhrasesViewModel
import io.github.jd1378.otphelper.ui.screens.SettingsViewModel

/**
 * OTPilot navigation: every screen is the tvnclr-ui version. Routes, arguments and deep links
 * are the same as upstream, so notifications and shortcuts keep working.
 */
fun NavGraphBuilder.otpilotNavGraph(
    upPress: () -> Unit,
    onNavigateToRoute: (String, Boolean, Boolean) -> Unit,
) {
  val scheme = OTPHELPER_APP_SCHEME
  composable(
      MainDestinations.HOME_ROUTE,
      deepLinks = listOf(navDeepLink { uriPattern = "$scheme://${MainDestinations.HOME_ROUTE}" }),
  ) {
    HomeScreen(onNavigateToRoute, hiltViewModel<OtpHomeViewModel>())
  }
  composable(
      MainDestinations.SETTINGS_ROUTE,
      deepLinks = listOf(navDeepLink { uriPattern = "$scheme://${MainDestinations.SETTINGS_ROUTE}" }),
  ) {
    SettingsScreen(onNavigateToRoute, upPress, hiltViewModel<SettingsViewModel>())
  }
  composable(MainDestinations.LANGUAGE_SELECTION_ROUTE) {
    LanguageScreen(upPress, hiltViewModel<LanguageSelectionViewModel>())
  }
  composable(MainDestinations.MODE_SETUP_ROUTE, arguments = listOf(navArgument("setup") { nullable = true })) { entry ->
    ModeScreen(onNavigateToRoute, upPress, hiltViewModel<ModeChooseViewModel>(), entry.arguments?.containsKey("setup") ?: false)
  }
  composable(
      MainDestinations.PERMISSIONS_SETUP_ROUTE,
      arguments = listOf(navArgument("setup") { nullable = true }),
      deepLinks = listOf(navDeepLink { uriPattern = "$scheme://${MainDestinations.PERMISSIONS_ROUTE}" }),
  ) { entry ->
    PermissionsScreen(onNavigateToRoute, upPress, hiltViewModel<PermissionsViewModel>(), entry.arguments?.containsKey("setup") ?: false)
  }
  composable(MainDestinations.ABOUT_ROUTE) { AboutScreen(upPress) }
  composable(MainDestinations.HISTORY_ROUTE) {
    HistoryScreen(onNavigateToRoute, upPress, hiltViewModel<HistoryViewModel>())
  }
  composable(
      "${MainDestinations.HISTORY_DETAIL_ROUTE}/{${NavArgs.HISTORY_ID}}",
      deepLinks =
          listOf(navDeepLink { uriPattern = "$scheme://${MainDestinations.HISTORY_DETAIL_ROUTE}/{${NavArgs.HISTORY_ID}}" }),
      arguments = listOf(navArgument(NavArgs.HISTORY_ID) { type = NavType.LongType }),
  ) { entry ->
    if (entry.arguments?.getLong(NavArgs.HISTORY_ID) == 0L) upPress()
    else HistoryDetailScreen(upPress, hiltViewModel<HistoryDetailViewModel>())
  }
  composable(MainDestinations.IGNORED_APP_LIST_ROUTE) {
    IgnoredListScreen(onNavigateToRoute, upPress, hiltViewModel<IgnoredAppListViewModel>())
  }
  composable(
      "${MainDestinations.IGNORED_APP_DETAIL_ROUTE}/{${NavArgs.PACKAGE_NAME}}",
      arguments = listOf(navArgument(NavArgs.PACKAGE_NAME) { type = NavType.StringType }),
  ) { entry ->
    if (entry.arguments?.getString(NavArgs.PACKAGE_NAME).isNullOrEmpty()) upPress()
    else IgnoredDetailScreen(upPress, hiltViewModel<IgnoredAppDetailViewModel>())
  }
  composable(MainDestinations.SENSITIVE_PHRASES_ROUTE) { SensitivePhrasesScreen(upPress, hiltViewModel<SensitivePhrasesViewModel>()) }
  composable(MainDestinations.IGNORED_PHRASES_ROUTE) { IgnoredPhrasesScreen(upPress, hiltViewModel<IgnoredPhrasesViewModel>()) }
  composable(MainDestinations.CLEANUP_PHRASES_ROUTE) { CleanupPhrasesScreen(upPress, hiltViewModel<CleanupPhrasesViewModel>()) }
  composable(MainDestinations.DETECTION_TEST_ROUTE) { DetectionTestScreen(upPress, hiltViewModel<DetectionTestViewModel>()) }
}
