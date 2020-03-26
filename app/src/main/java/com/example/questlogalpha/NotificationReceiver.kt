package com.example.questlogalpha

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent == null) {
            Log.e(TAG, "Intent is null")
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            return
        }

        val id = intent.getIntExtra(NOTIFICATION_ID, NotificationId)

        Log.d(TAG, "id: $id")
        Log.d(TAG, "intent URI" + intent.toUri(0))

        when (intent.action) {
            ACTION_DISMISS -> {
                Log.d(TAG, "onReceive: ACTION_DISMISS")
                handleActionDismiss(context, id)
            }
            ACTION_SNOOZE -> {
                Log.d(TAG, "onReceive: ACTION_SNOOZE")

                // todo handle action snooze
                handleActionDismiss(context, id)
            }
            else -> {
                Log.d(TAG, "onReceive: else")
                val notification: Notification = intent.getParcelableExtra(NOTIFICATION)
                val notificationManager = NotificationManagerCompat.from(context!!)
                notificationManager.notify(id, notification)
            }
        }
    }

    /** Handles action Dismiss in the provided background thread. */
    private fun handleActionDismiss(context: Context?, notificationId: Int) {
        Log.d(TAG, "handleActionDismiss()")
        val notificationManagerCompat = NotificationManagerCompat.from(context!!)
        notificationManagerCompat.cancel(notificationId)
    }

    companion object {

        const val NotificationId = 888
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
        val ACTION_DISMISS = "com.example.questlogalpha.handlers.action.DISMISS"
        val ACTION_SNOOZE = "com.example.questlogalpha.handlers.action.SNOOZE"

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: NotificationReceiver"
    }
}
