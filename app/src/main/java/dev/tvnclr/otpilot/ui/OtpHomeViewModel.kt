package dev.tvnclr.otpilot.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jd1378.otphelper.NotificationListener
import io.github.jd1378.otphelper.SmsListener
import io.github.jd1378.otphelper.UserSettings
import io.github.jd1378.otphelper.copy
import io.github.jd1378.otphelper.repository.DetectedCodeRepository
import io.github.jd1378.otphelper.repository.UserSettingsRepository
import io.github.jd1378.otphelper.utils.NotificationHelper
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import dev.tvnclr.otpilot.SensitiveNotifAccess

data class HomeChecks(
    val notifListener: Boolean = false,
    val smsAccess: Boolean = false,
    val postNotifs: Boolean = true,
    val sensitive: SensitiveNotifAccess.State = SensitiveNotifAccess.State.NOT_NEEDED,
    val unrestricted: Boolean = true,
    val loaded: Boolean = false,
)

@Stable
@HiltViewModel
class OtpHomeViewModel
@Inject
constructor(
    userSettingsRepository: UserSettingsRepository,
    detectedCodeRepository: DetectedCodeRepository,
) : ViewModel() {

  val userSettings: StateFlow<UserSettings> =
      userSettingsRepository.userSettings.stateIn(
          viewModelScope,
          SharingStarted.Eagerly,
          UserSettings.getDefaultInstance().copy { isSetupFinished = true },
      )

  val lastCodes = detectedCodeRepository.get(pageSize = 1).cachedIn(viewModelScope)

  private val _checks = MutableStateFlow(HomeChecks())
  val checks: StateFlow<HomeChecks> = _checks

  /** Re-reads every permission; called whenever the screen resumes. */
  fun refresh(context: Context) {
    val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    _checks.value =
        HomeChecks(
            notifListener = NotificationListener.isNotificationListenerServiceEnabled(context),
            smsAccess = SmsListener.hasSmsPermission(context),
            postNotifs = NotificationHelper.hasNotifPermission(context),
            sensitive = SensitiveNotifAccess.state(context),
            unrestricted = power.isIgnoringBatteryOptimizations(context.packageName),
            loaded = true,
        )
  }

  fun sendTestNotif(context: Context) = NotificationHelper.sendTestNotif(context)
}
