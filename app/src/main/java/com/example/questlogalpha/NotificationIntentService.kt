package com.example.questlogalpha

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.TimeUnit


class NotificationIntentService : IntentService("NotificationIntentService") {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")

        // Cancel Notification
        // this is what they do in the example, so /shrug. This doesn't actually seem to do anything
  //      handleActionDismiss(888)
    }

    override fun onHandleIntent(intent: Intent?) {

        Log.d(TAG, "onHandleIntent()")

        if (intent == null) {
            Log.e(TAG, "Intent is null")
            Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            return
        }

        val id = intent.getIntExtra(NOTIFICATION_ID, 888)

        Log.d(TAG, "id: $id")
        Log.d(TAG, "intent URI" + intent.toUri(0))

        when (intent.action) {
            ACTION_DISMISS -> {
                Log.d(TAG, "onHandleIntent: ACTION_DISMISS")
                handleActionDismiss(id)
            }
            ACTION_SNOOZE -> {
                Log.d(TAG, "onHandleIntent: ACTION_SNOOZE")

                // todo handle action snooze
                handleActionDismiss(id)
            }
            else -> {
                Log.d(TAG, "onHandleIntent: else")
                val notification: Notification = intent.getParcelableExtra(NOTIFICATION)

                val notificationManager = NotificationManagerCompat.from(applicationContext!!)
                notificationManager.notify(id, notification)
            }
        }
    }

    /** Handles action Dismiss in the provided background thread. */
    private fun handleActionDismiss(id: Int) {

        val notificationManager = NotificationManagerCompat.from(applicationContext!!)
        notificationManager.cancel(id)
        Log.d(TAG, "handleActionDismiss()")
    }

    // /** Handles action Snooze in the provided background thread. */
// private fun handleActionSnooze() {
//     Log.d(TAG, "handleActionSnooze()")
//     // You could use NotificationManager.getActiveNotifications() if you are targeting SDK 23
//     // and above, but we are targeting devices with lower SDK API numbers, so we saved the
//     // builder globally and get the notification back to recreate it later.
//     var notificationCompatBuilder: NotificationCompat.Builder =
//         GlobalNotificationBuilder.getNotificationCompatBuilderInstance()
//     // Recreate builder from persistent state if app process is killed
//     if (notificationCompatBuilder == null) { // Note: New builder set globally in the method
//         notificationCompatBuilder = recreateBuilderWithBigTextStyle()
//     }
//     val notification: Notification?
//     notification = notificationCompatBuilder.build()
//     if (notification != null) {
//         val notificationManagerCompat =
//             NotificationManagerCompat.from(applicationContext)
//         notificationManagerCompat.cancel(channelId)
//         try {
//             Thread.sleep(SNOOZE_TIME)
//         } catch (ex: InterruptedException) {
//             Thread.currentThread().interrupt()
//         }
//         notificationManagerCompat.notify(channelId, notification)
//     }
// }


    companion object {
        const val NotificationId = 888
        const val ACTION_DISMISS = "com.example.questlogalpha.handlers.action.DISMISS"
        const val ACTION_SNOOZE = "com.example.questlogalpha.handlers.action.SNOOZE"
        private val SNOOZE_TIME = TimeUnit.SECONDS.toMillis(5)
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: NotificationIntentService"
    }
}