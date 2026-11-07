package dev.tvnclr.otpilot

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.jd1378.otphelper.R
import io.github.jd1378.otphelper.utils.AppLogger
import io.github.jd1378.otphelper.utils.NotificationHelper
import java.util.concurrent.TimeUnit

/**
 * OTPilot: Android 15+ hides notifications that contain one-time codes from notification
 * listeners that don't hold RECEIVE_SENSITIVE_NOTIFICATIONS. That access can only be granted
 * through the app-op of the same name (ADB or root). This object reads the real state of that
 * app-op and, on rooted devices, grants it to OTPilot itself.
 */
object SensitiveNotifAccess {
  private const val TAG = "SensitiveNotifAccess"
  private const val OP_STR = "android:receive_sensitive_notifications"
  private const val OP_NAME = "RECEIVE_SENSITIVE_NOTIFICATIONS"
  private const val NOTIFY_ID = 0x07B1_0001

  enum class State {
    /** Android < 15: there is no restriction to deal with. */
    NOT_NEEDED,
    ALLOWED,
    BLOCKED,
    /** The system did not let us read the app-op. */
    UNKNOWN,
  }

  fun isRestricted(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

  fun state(context: Context): State {
    if (!isRestricted()) return State.NOT_NEEDED
    return try {
      val appOps = context.getSystemService(AppOpsManager::class.java)
      val mode = appOps.unsafeCheckOpNoThrow(OP_STR, Process.myUid(), context.packageName)
      if (mode == AppOpsManager.MODE_ALLOWED) State.ALLOWED else State.BLOCKED
    } catch (e: Throwable) {
      AppLogger.w(TAG, "could not read $OP_STR: ${e.message}")
      State.UNKNOWN
    }
  }

  /** Grants the app-op to ourselves through `su`. Returns true if it is allowed afterwards. */
  fun grantWithRoot(context: Context): Boolean {
    if (!isRestricted()) return true
    val user = Process.myUid() / 100_000
    val ok = runAsRoot("cmd appops set --user $user ${context.packageName} $OP_NAME allow")
    val after = state(context)
    AppLogger.i(TAG, "grantWithRoot: su=$ok, state=$after")
    return after == State.ALLOWED || (ok && after == State.UNKNOWN)
  }

  /**
   * Makes sure the access is in place. If it is blocked, tries root; if that fails too and
   * [notifyIfBlocked] is set, posts a notification so the failure is never silent.
   */
  fun ensure(context: Context, notifyIfBlocked: Boolean): State {
    val current = state(context)
    if (current == State.ALLOWED || current == State.NOT_NEEDED) {
      cancelBlockedNotif(context)
      return current
    }
    if (grantWithRoot(context)) {
      cancelBlockedNotif(context)
      return State.ALLOWED
    }
    if (notifyIfBlocked) sendBlockedNotif(context)
    return State.BLOCKED
  }

  @SuppressLint("NewApi") // only reached on Android 15+ (see isRestricted)
  private fun runAsRoot(command: String): Boolean {
    return try {
      val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
      if (!process.waitFor(20, TimeUnit.SECONDS)) {
        process.destroy()
        AppLogger.w(TAG, "su timed out")
        return false
      }
      val output = process.inputStream.bufferedReader().readText().trim()
      if (output.isNotEmpty()) AppLogger.d(TAG, "su output: $output")
      process.exitValue() == 0
    } catch (e: Throwable) {
      AppLogger.w(TAG, "su not available: ${e.message}")
      false
    }
  }

  @SuppressLint("MissingPermission")
  private fun sendBlockedNotif(context: Context) {
    if (!NotificationHelper.hasNotifPermission(context)) return
    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    val pending =
        PendingIntent.getActivity(
            context, NOTIFY_ID, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val notif =
        NotificationCompat.Builder(context, context.getString(R.string.permission_revoked_channel_id))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.otpilot_sensitive_blocked_title))
            .setContentText(context.getString(R.string.otpilot_sensitive_blocked_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.otpilot_sensitive_blocked_text)))
            .setContentIntent(pending)
            .setCategory(Notification.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()
    NotificationManagerCompat.from(context).notify(NOTIFY_ID, notif)
  }

  private fun cancelBlockedNotif(context: Context) {
    NotificationManagerCompat.from(context).cancel(NOTIFY_ID)
  }
}
