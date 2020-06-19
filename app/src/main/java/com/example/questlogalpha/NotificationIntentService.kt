package com.example.questlogalpha

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.data.StoredNotification
import java.util.concurrent.TimeUnit


class NotificationIntentService : IntentService("NotificationIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent()")
        val dataSource = QuestLogDatabase.getInstance(this).questLogDatabaseDao

        if (intent == null) {
            Log.e(TAG, "Intent is null")
            Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            return
        }

        val id = intent.getIntExtra(NOTIFICATION_ID, 888)
        val questId = intent.getStringExtra(QUEST_ID)

        Log.d(TAG, "id: $id")
        Log.d(TAG, "questId: $questId")
        Log.d(TAG, "intent URI" + intent.toUri(0))

        val quest = dataSource.getQuestById(questId)
        if(quest == null) Log.e(TAG, "quest id $questId not found in the database!")
        else {
            val success = removeFiredNotification(quest, id)
            if(!success) Log.e(TAG, "A notification ($id) fired that was not found in the database for quest $questId!!!")
            else dataSource.setNotifications(questId, quest.notifications)
        }

        when (intent.action) {
            ACTION_DISMISS_SWIPE -> {
                Log.d(TAG, "onHandleIntent: ACTION_DISMISS_SWIPE")
                handleActionDismiss(id)
            }
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

    /** Removes the [StoredNotification] with [id] from the [quest]'s list of notifications.
     * @return true if the [id] was found, false if not found
     * */
    private fun removeFiredNotification(quest: Quest, id: Int) : Boolean {
        val notifications = arrayListOf<StoredNotification>()
        notifications.addAll(quest.notifications)
        for (notification in notifications) {
            if (notification.id == id) {
                Log.d(TAG,"Notification ${notification.id} has just fired. Removing from database.")
                quest.notifications.remove(notification)
                return true
            }
        }
        return false
    }

    /** Handles action Dismiss for notification with [id] in the provided background thread. */
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
        const val ACTION_DISMISS_SWIPE = "com.example.questlogalpha.handlers.action.DISMISS_SWIPE"
        const val ACTION_SNOOZE = "com.example.questlogalpha.handlers.action.SNOOZE"

        private val SNOOZE_TIME = TimeUnit.SECONDS.toMillis(5)
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
        var QUEST_ID = "quest-id"

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: NotificationIntentService"
    }
}