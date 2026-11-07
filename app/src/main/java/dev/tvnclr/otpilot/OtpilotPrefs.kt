package dev.tvnclr.otpilot

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.otpilotStore by preferencesDataStore(name = "otpilot")

/** OTPilot-only preferences, kept apart from upstream's UserSettings proto to keep merges clean. */
object OtpilotPrefs {
  private val RECENTS_PREVIEW = booleanPreferencesKey("recents_preview")

  /** Recent-apps preview is only controllable on Android 13+ (setRecentsScreenshotEnabled). */
  val recentsPreviewSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

  /** Off by default: the app shows codes, so its recents thumbnail stays blank. */
  fun recentsPreview(context: Context): Flow<Boolean> =
      context.applicationContext.otpilotStore.data.map { it[RECENTS_PREVIEW] ?: false }

  suspend fun setRecentsPreview(context: Context, enabled: Boolean) {
    context.applicationContext.otpilotStore.edit { it[RECENTS_PREVIEW] = enabled }
  }

  fun applyRecentsPreview(activity: Activity, enabled: Boolean) {
    if (recentsPreviewSupported) activity.setRecentsScreenshotEnabled(enabled)
  }
}
