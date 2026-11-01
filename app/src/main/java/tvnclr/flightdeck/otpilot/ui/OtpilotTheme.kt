package tvnclr.flightdeck.otpilot.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import tvnclr.flightdeck.ui.FdThemePrefs
import tvnclr.flightdeck.ui.FdThemeSettings
import tvnclr.flightdeck.ui.FlightdeckTheme

/** App theme: Flightdeck UI driven by the stored appearance preferences. */
@Composable
fun OtpilotTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val flow = remember { FdThemePrefs.flow(context) }
  val settings by flow.collectAsState(initial = FdThemeSettings())
  FlightdeckTheme(settings, content)
}
