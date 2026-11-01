package tvnclr.flightdeck.otpilot.ui

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import io.github.jd1378.otphelper.ui.navigation.MainDestinations
import io.github.jd1378.otphelper.OTPHELPER_APP_SCHEME
import io.github.jd1378.otphelper.ui.navigation.addAboutGraph
import io.github.jd1378.otphelper.ui.navigation.addCleanupPhrasesGraph
import io.github.jd1378.otphelper.ui.navigation.addDetectionTestGraph
import io.github.jd1378.otphelper.ui.navigation.addHistoryDetailGraph
import io.github.jd1378.otphelper.ui.navigation.addHistoryGraph
import io.github.jd1378.otphelper.ui.navigation.addIgnoredAppDetailGraph
import io.github.jd1378.otphelper.ui.navigation.addIgnoredAppListGraph
import io.github.jd1378.otphelper.ui.navigation.addIgnoredPhrasesGraph
import io.github.jd1378.otphelper.ui.navigation.addLanguageSelectionGraph
import io.github.jd1378.otphelper.ui.navigation.addModeChooseGraph
import io.github.jd1378.otphelper.ui.navigation.addPermissionsGraph
import io.github.jd1378.otphelper.ui.navigation.addSensitivePhrasesGraph
import io.github.jd1378.otphelper.ui.screens.SettingsViewModel

/**
 * OTPilot navigation. Screens rewritten in Flightdeck UI are registered here; the rest still use
 * the upstream composables until they are rewritten too.
 */
fun NavGraphBuilder.otpilotNavGraph(
    upPress: () -> Unit,
    onNavigateToRoute: (String, Boolean, Boolean) -> Unit,
) {
  composable(
      MainDestinations.HOME_ROUTE,
      deepLinks = listOf(navDeepLink { uriPattern = "$OTPHELPER_APP_SCHEME://${MainDestinations.HOME_ROUTE}" }),
  ) {
    HomeScreen(onNavigateToRoute, hiltViewModel<OtpHomeViewModel>())
  }
  composable(MainDestinations.SETTINGS_ROUTE) {
    SettingsScreen(onNavigateToRoute, upPress, hiltViewModel<SettingsViewModel>())
  }

  // not rewritten yet
  addLanguageSelectionGraph(upPress)
  addIgnoredAppListGraph(onNavigateToRoute, upPress)
  addIgnoredAppDetailGraph(upPress = upPress)
  addModeChooseGraph(onNavigateToRoute, upPress)
  addPermissionsGraph(onNavigateToRoute, upPress)
  addAboutGraph(upPress)
  addHistoryGraph(onNavigateToRoute = onNavigateToRoute, upPress = upPress)
  addHistoryDetailGraph(upPress = upPress)
  addSensitivePhrasesGraph(upPress = upPress)
  addIgnoredPhrasesGraph(upPress = upPress)
  addCleanupPhrasesGraph(upPress = upPress)
  addDetectionTestGraph(upPress = upPress)
}
