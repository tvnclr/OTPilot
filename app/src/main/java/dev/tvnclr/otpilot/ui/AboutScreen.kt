package dev.tvnclr.otpilot.ui

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
import dev.tvnclr.ui.Fd
import dev.tvnclr.ui.TvCard
import dev.tvnclr.ui.TvDivider
import dev.tvnclr.ui.TvNote
import dev.tvnclr.ui.TvPage
import dev.tvnclr.ui.TvRow
import dev.tvnclr.ui.TvSection
import dev.tvnclr.ui.TvSpace
import dev.tvnclr.ui.TvType

private const val REPO = "https://github.com/tvnclr/OTPilot"
private const val UPSTREAM = "https://github.com/jd1378/otphelper"

@Composable
fun AboutScreen(upPress: () -> Unit) {
  val uri = LocalUriHandler.current
  val context = LocalContext.current
  val c = Tv.colors
  val open: (String) -> Unit = { uri.openUriSafely(context, it) }

  TvPage(title = stringResource(R.string.about), onBack = upPress) {
    TvCard {
      Column(
          Modifier.fillMaxWidth().padding(vertical = TvSpace.xl, horizontal = TvSpace.l),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(TvSpace.s),
      ) {
        Image(painterResource(R.drawable.logo_round), null, Modifier.size(96.dp))
        Text(stringResource(R.string.otpilot_app_name), style = TvType.display, color = c.textPrimary)
        Text(stringResource(R.string.otp_about_tagline), style = TvType.body, color = c.textSecondary, textAlign = TextAlign.Center)
      }
    }

    TvSection(stringResource(R.string.otp_about_version), Icons.Rounded.Info) {
      TvRow(stringResource(R.string.otp_about_version), icon = Icons.Rounded.Tag, trailing = {
        Text(BuildConfig.VERSION_NAME, style = TvType.mono, color = c.textSecondary)
      })
      TvDivider()
      TvRow(stringResource(R.string.otp_about_license), subtitle = stringResource(R.string.license_type), icon = Icons.Rounded.Gavel)
    }

    TvSection(stringResource(R.string.otp_about_source), Icons.Rounded.Code) {
      TvRow(stringResource(R.string.otp_about_source), subtitle = "github.com/tvnclr/OTPilot", icon = Icons.Rounded.Code, onClick = { open(REPO) })
      TvDivider()
      TvRow(stringResource(R.string.otp_about_privacy), icon = Icons.Rounded.Shield, onClick = { open("$REPO#privacy") })
      TvDivider()
      TvRow(stringResource(R.string.otp_about_upstream), subtitle = "github.com/jd1378/otphelper", icon = Icons.Rounded.Link, onClick = { open(UPSTREAM) })
      TvNote(stringResource(R.string.otp_about_fork))
    }
  }
}
