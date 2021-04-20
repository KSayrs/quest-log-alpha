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
package com.example.questlogalpha.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.System.getString
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.questlogalpha.R
import androidx.fragment.app.Fragment
import com.example.questlogalpha.BuildConfig
import com.example.questlogalpha.Util
import com.example.questlogalpha.data.StoredAction
import com.example.questlogalpha.data.StoredIntent
import com.example.questlogalpha.data.StoredNotification
import com.example.questlogalpha.data.StoredPendingIntent
import com.example.questlogalpha.vieweditquest.ViewEditQuestFragment
import java.time.*
import kotlin.math.abs


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

    /** Formats the display string for the reminder time.
     * Set [handleMonthOffset] to true is there has been a conversion to and from an Instant after using the date picker.
     * The date pickers increments months differently, so there is an offset to be accounted for. */
    fun formatNotificationText(dueDate: ZonedDateTime, notificationTime: ZonedDateTime, handleMonthOffset: Boolean = false): String {

        Log.d(TAG, "formatNotificationText: dueDate: $dueDate")
        Log.d(TAG, "formatNotificationText: notificationTime: $notificationTime")

        val offset = if(handleMonthOffset) notificationTime.minusMonths(1) else notificationTime

        var remainingTime = dueDate.toInstant().epochSecond - offset.toInstant().epochSecond

        val weeksDifference = remainingTime / SECONDS_PER_WEEK
        Log.d(TAG, "weeksDifference: + $weeksDifference")

        if(weeksDifference >= 1) { remainingTime %= SECONDS_PER_WEEK
        }

        val daysDifference = remainingTime / SECONDS_PER_DAY
        Log.d(TAG, "daysInDifference: + $daysDifference")

        if(daysDifference >= 1) { remainingTime %= SECONDS_PER_DAY
        }

        val hoursDifference = remainingTime / SECONDS_PER_HOUR
        Log.d(TAG, "hoursDifference: + $hoursDifference")

        if(hoursDifference >= 1) { remainingTime %= SECONDS_PER_HOUR
        }

        val minutesDifference = remainingTime / SECONDS_PER_MINUTE
        Log.d(TAG, "minutesDifference: + $minutesDifference")

       return buildText(
           0,
           weeksDifference,
           daysDifference,
           hoursDifference,
           minutesDifference
       )
    }

    /** Builds the display string for the custom notification time. */
    private fun buildText(years: Int, weeks: Long, days: Long, hours: Long, minutes: Long): String {
        var text = ""

        if(years != 0) {
            text += "${abs(years)}y "
        }
        if(weeks != 0L) {
            text += "${abs(weeks)}w "
        }
        if(days != 0L) {
            text += "${abs(days)}d "
        }
        if(hours != 0L) {
            text += "${abs(hours)}h "
        }
        if(minutes != 0L) {
            text += "${abs(minutes)}m "
        }

        if(text == "") { text = "At date/time" }
        else text += " before"

        //  if(text.length > 8) {
        //      val substrings = text.split(" ")
        //      var currentLength = 0
        //      for(substring in substrings) {
        //          if(substring.length + currentLength + 1 <= 8) {  // +1 for the space at the end that's removed for delimiting
        //              currentLength += substring.length + 1;
        //          } else {
        //              return text.substring(0, currentLength)
        //          }
        //      }
        //      return text
        //  }

        return text
    }

    /** Schedule a [notification] with [notificationId] to happen at [notificationTime] (milliseconds). */
    fun scheduleNotification(notification: Notification, notificationTime: Long, notificationId: Int, questId: String, context: Context) : PendingIntent
    {
        Log.d(TAG, "scheduleNotification ************************ ")
        Log.d(TAG, " notificationid: $notificationId")
        Log.d(TAG, " notification: $notification")
        Log.d(TAG, " notificationTime: $notificationTime")
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra(NotificationIntentService.NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(NotificationIntentService.NOTIFICATION, notification)
        notificationIntent.putExtra(NotificationIntentService.QUEST_ID, questId)
        return PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /** Build and return a [Notification] out of a stored [notification]. */
    fun getNotification(notification: StoredNotification, questId: String, context: Context?) : Notification
    {
        // build notification channel
        val notificationChannelId = NotificationUtil.createNotificationChannel(
            context!!,
            ViewEditQuestFragment.channelId,
            context.getString(R.string.familiar_quest_reminders),
            context.getString(R.string.familiar_quest_reminders_description),
            NotificationManager.IMPORTANCE_DEFAULT,
            true,
            Notification.VISIBILITY_PUBLIC
        )

        // to make a new notification id, add
        // builder.addExtras(NOTIFICATION_ID, number)
        // and then get it in the receiver.

        val builder = NotificationCompat.Builder(context, notificationChannelId!!)

        // iterate through actions and build them
        for (action in notification.actions) {
            val intent = makeGenericIntent(action.intent, questId, context)
            if (BuildConfig.DEBUG && intent == null) {
                error("intent for action ${action.id} is null")
            }
            val pendingIntent = PendingIntent.getService(context, action.intent.requestCode, intent!!, action.intent.flags)
            builder.addAction(action.iconPath, action.title, pendingIntent)
        }
        builder.setContentTitle(notification.contentTitle)
        builder.setContentText(notification.contentText)
        builder.setSmallIcon(notification.icon)
        builder.setLargeIcon(Util.drawableToBitmap(context.resources.getDrawable(notification.bigIcon, context.theme)))
        builder.priority = notification.channelPriority
        builder.setAutoCancel(notification.autoCancel)
        builder.setWhen(notification.notificationTime)

        // make the delete intent
        val deleteIntent = makeGenericIntent(notification.deleteIntent, questId, context)
        if(deleteIntent != null) {
            val deletePendingIntent = PendingIntent.getService(
                context,
                notification.deleteIntent!!.requestCode,
                deleteIntent,
                notification.deleteIntent!!.flags
            )
            builder.setDeleteIntent(deletePendingIntent)
        }

        Log.d(
            TAG, "getNotification ************************ " +
                    "\n contentTitle: ${notification.contentTitle}" +
                    "\n contentText: ${notification.contentText}" +
                    "\n priority: ${notification.channelPriority}" +
                    "\n icon path: ${notification.icon}" +
                    "\n big icon path: ${notification.bigIcon}" +
                    "\n setAutoCancel: ${notification.autoCancel}" +
                    "\n deleteIntent: ${notification.deleteIntent}" +
                    "\n actions: ${notification.actions}")

        return builder.build()
    }

    /** Make and return an [Intent] from a [storedPendingIntent]s data. */
    private fun makeGenericIntent(storedPendingIntent: StoredPendingIntent?, questId: String, context: Context?): Intent? {
        if(storedPendingIntent == null) return null
        val intent = Intent(context, NotificationIntentService::class.java)
        intent.action = storedPendingIntent.intent.action
        intent.putExtra(NotificationIntentService.NOTIFICATION_ID, storedPendingIntent.intent.id)
        intent.putExtra(NotificationIntentService.QUEST_ID, questId)
        for(extra in storedPendingIntent.intent.extras)
        {
            Log.d(TAG, "makeGenericIntent: extra: $extra")
            intent.putExtra(extra.key, extra.value)
        }
        return intent
    }

    /** Set an alarm using the data in [alarmData]. */
    fun setAlarm(alarmManager: AlarmManager, alarmData: AlarmData)
    {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmData.notificationTime, alarmData.pendingIntent)
    }

    const val SECONDS_PER_WEEK:Long = 604800
    const val SECONDS_PER_DAY:Long = 86400
    const val SECONDS_PER_HOUR:Long = 3600
    const val SECONDS_PER_MINUTE:Long = 60

    // -------------------------- log tag ------------------------------ //
    private const val TAG:String = "KSLOG: NotificationUtil"
}

data class AlarmData(
    var pendingIntent: PendingIntent,
    var notificationTime: Long
)
