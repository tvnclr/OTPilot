package tvnclr.flightdeck.otpilot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jd1378.otphelper.BuildConfig
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.utils.openUriSafely
import tvnclr.flightdeck.ui.Fd
import tvnclr.flightdeck.ui.FdCard
import tvnclr.flightdeck.ui.FdDivider
import tvnclr.flightdeck.ui.FdNote
import tvnclr.flightdeck.ui.FdPage
import tvnclr.flightdeck.ui.FdRow
import tvnclr.flightdeck.ui.FdSection
import tvnclr.flightdeck.ui.FdSpace
import tvnclr.flightdeck.ui.FdType

private const val REPO = "https://github.com/tvnclr/OTPilot"
private const val UPSTREAM = "https://github.com/jd1378/otphelper"

@Composable
fun AboutScreen(upPress: () -> Unit) {
  val uri = LocalUriHandler.current
  val context = LocalContext.current
  val c = Fd.colors
  val open: (String) -> Unit = { uri.openUriSafely(context, it) }

  FdPage(title = stringResource(R.string.about), onBack = upPress) {
    FdCard {
      Column(
          Modifier.fillMaxWidth().padding(vertical = FdSpace.xl, horizontal = FdSpace.l),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(FdSpace.s),
      ) {
        Image(painterResource(R.drawable.logo_round), null, Modifier.size(96.dp))
        Text(stringResource(R.string.otpilot_app_name), style = FdType.display, color = c.textPrimary)
        Text(stringResource(R.string.otp_about_tagline), style = FdType.body, color = c.textSecondary, textAlign = TextAlign.Center)
      }
    }

    FdSection(stringResource(R.string.otp_about_version), Icons.Rounded.Info) {
      FdRow(stringResource(R.string.otp_about_version), icon = Icons.Rounded.Tag, trailing = {
        Text(BuildConfig.VERSION_NAME, style = FdType.mono, color = c.textSecondary)
      })
      FdDivider()
      FdRow(stringResource(R.string.otp_about_license), subtitle = stringResource(R.string.license_type), icon = Icons.Rounded.Gavel)
    }

    FdSection(stringResource(R.string.otp_about_source), Icons.Rounded.Code) {
      FdRow(stringResource(R.string.otp_about_source), subtitle = "github.com/tvnclr/OTPilot", icon = Icons.Rounded.Code, onClick = { open(REPO) })
      FdDivider()
      FdRow(stringResource(R.string.otp_about_privacy), icon = Icons.Rounded.Shield, onClick = { open("$REPO#privacy") })
      FdDivider()
      FdRow(stringResource(R.string.otp_about_upstream), subtitle = "github.com/jd1378/otphelper", icon = Icons.Rounded.Link, onClick = { open(UPSTREAM) })
      FdNote(stringResource(R.string.otp_about_fork))
    }
  }
}
