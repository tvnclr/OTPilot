package dev.tvnclr.otpilot.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.tvnclr.ui.TvThemePrefs
import dev.tvnclr.ui.TvThemeSettings
import dev.tvnclr.ui.TvnclrTheme

/** App theme: tvnclr-ui driven by the stored appearance preferences. */
@Composable
fun OtpilotTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val flow = remember { TvThemePrefs.flow(context) }
  val settings by flow.collectAsState(initial = TvThemeSettings())
  TvnclrTheme(settings, content)
}
