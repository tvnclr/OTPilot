package tvnclr.flightdeck.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FdThemeSettings(
    val mode: FdThemeMode = FdThemeMode.SYSTEM,
    val accent: FdAccent = FdAccent.default,
    val background: FdBackgroundStyle = FdBackgroundStyle.ORBS,
)

private val Context.flightdeckStore by preferencesDataStore(name = "flightdeck_ui")

/** Appearance preferences, stored apart from the app's own settings. */
object FdThemePrefs {
  private val MODE = stringPreferencesKey("theme_mode")
  private val ACCENT = stringPreferencesKey("accent")
  private val BACKGROUND = stringPreferencesKey("background")

  fun flow(context: Context): Flow<FdThemeSettings> =
      context.applicationContext.flightdeckStore.data.map { p ->
        FdThemeSettings(
            mode = p[MODE]?.let { runCatching { FdThemeMode.valueOf(it) }.getOrNull() } ?: FdThemeMode.SYSTEM,
            accent = p[ACCENT]?.let { runCatching { FdAccent.valueOf(it) }.getOrNull() } ?: FdAccent.default,
            background = p[BACKGROUND]?.let { runCatching { FdBackgroundStyle.valueOf(it) }.getOrNull() } ?: FdBackgroundStyle.ORBS,
        )
      }

  suspend fun setMode(context: Context, mode: FdThemeMode) {
    context.applicationContext.flightdeckStore.edit { it[MODE] = mode.name }
  }

  suspend fun setAccent(context: Context, accent: FdAccent) {
    context.applicationContext.flightdeckStore.edit { it[ACCENT] = accent.name }
  }

  suspend fun setBackground(context: Context, style: FdBackgroundStyle) {
    context.applicationContext.flightdeckStore.edit { it[BACKGROUND] = style.name }
  }
}
