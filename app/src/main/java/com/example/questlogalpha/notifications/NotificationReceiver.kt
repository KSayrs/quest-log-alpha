package com.example.questlogalpha.notifications

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

        val id = intent.getIntExtra(
            NOTIFICATION_ID,
            NotificationId
        )

        Log.d(TAG, "id: $id")
        Log.d(TAG, "intent URI" + intent.toUri(0))

        when (intent.action) {
            NotificationIntentService.ACTION_DISMISS -> {
                Log.d(TAG, "onReceive: ACTION_DISMISS")
            }
            NotificationIntentService.ACTION_SNOOZE -> {
                Log.d(TAG, "onReceive: ACTION_SNOOZE")
            }
            else -> {
                Log.d(TAG, "onReceive: else: ${intent.action}")
                val notification: Notification = intent.getParcelableExtra(NOTIFICATION)
                val notificationManager = NotificationManagerCompat.from(context!!)
                notificationManager.notify(id, notification)
            }
        }
    }

    companion object {

        const val NotificationId = 888
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: NotificationReceiver"
    }
}
