/*
 * Copyright (C) 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.questlogalpha

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.questlogalpha.data.StoredAction
import com.example.questlogalpha.data.StoredIntent
import com.example.questlogalpha.data.StoredPendingIntent


/**
 * Simplifies common [android.app.Notification] tasks.
 */
object NotificationUtil {

    fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String,
        channelImportance: Int,
        channelEnableVibrate: Boolean,
        channelLockscreenVisibility: Int
    ): String? {
        // NotificationChannels are required for Notifications on O (API 26) and above.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Initializes NotificationChannel.
            val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
            notificationChannel.description = channelDescription
            notificationChannel.enableVibration(channelEnableVibrate)
            notificationChannel.lockscreenVisibility = channelLockscreenVisibility
            // Adds NotificationChannel to system. Attempting to create an existing notification
            // channel with its original values performs no operation, so it's safe to perform the
            // below sequence.
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            channelId
        } else {
            Log.w(TAG, "createNotificationChannel returning null")
            // Returns null for pre-O (26) devices.
            null
        }
    }

    fun createStoredSnoozeAction() : StoredAction {
        val extras = HashMap<String, Int>()
        extras[NotificationIntentService.NOTIFICATION_ID] = NotificationIntentService.NotificationId

        val storedIntent = StoredIntent(NotificationIntentService.ACTION_SNOOZE, extras, NotificationIntentService.NotificationId)
        val storedPendingIntent = StoredPendingIntent(0, storedIntent, 0)
        return StoredAction(android.R.drawable.ic_popup_reminder, "Snooze", storedPendingIntent)
    }

    // Dismiss Action.
    fun createDismissAction(context: Context) : NotificationCompat.Action {
        val dismissIntent = Intent(context, NotificationIntentService::class.java)
        dismissIntent.action = NotificationIntentService.ACTION_DISMISS
        dismissIntent.putExtra(NotificationIntentService.NOTIFICATION_ID, NotificationIntentService.NotificationId)

        val dismissPendingIntent = PendingIntent.getService(context, NotificationIntentService.NotificationId, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT )

       // return  dismissPendingIntent
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_popup_reminder,
            context.getString(R.string.dismiss),
            dismissPendingIntent
        ).build()
    }

    /** Set an alarm using the data in [alarmData]. */
    fun setAlarm(alarmManager: AlarmManager, alarmData: AlarmData)
    {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmData.notificationTime, alarmData.pendingIntent)
    }

    // -------------------------- log tag ------------------------------ //
    private const val TAG:String = "KSLOG: NotificationUtil"
}

data class AlarmData(
    var pendingIntent: PendingIntent,
    var notificationTime: Long
)
