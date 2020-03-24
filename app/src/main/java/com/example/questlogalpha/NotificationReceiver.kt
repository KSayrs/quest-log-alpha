package com.example.questlogalpha

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.TimeUnit

class NotificationReceiver : IntentService("NotificationReceiver") {

   // override fun onCreate() {
   //     val intent = Intent(this, Quest::class.java)
   //     val pattern = longArrayOf(0, 300, 0)
   //     val pi = PendingIntent.getActivity(this, request_code, intent, 0) // why is the request code 668
   //     val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, Familiar_Alerts_Channel_Id)
   //         .setSmallIcon(R.drawable.ic_popup_reminder)
   //         .setContentTitle("Quest Log Alpha")
   //         .setContentText("temporary text")
   //         .setVibrate(pattern)
   //         .setAutoCancel(true)
   //         .setContentIntent(pi)
   //         .setDefaults(Notification.DEFAULT_SOUND)
//
   //     val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
   //     mNotificationManager.notify(channelId, mBuilder.build())
   // }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent(): $intent")
        if (intent != null) {
            val action = intent.action
            if (ACTION_DISMISS == action) {
                Log.d(TAG, "onHandleIntent: ACTION_DISMISS")
                handleActionDismiss()
            } else if (ACTION_SNOOZE == action) {
                Log.d(TAG, "onHandleIntent: ACTION_SNOOZE")
                // todo handle action snooze
                handleActionDismiss()
               // handleActionSnooze()
            }
        }
    }

    /** Handles action Dismiss in the provided background thread. */
    private fun handleActionDismiss() {
        Log.d(TAG, "handleActionDismiss()")
        val notificationManagerCompat =
            NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.cancel(NotificationId)
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


    private companion object {
        const val Familiar_Alerts_Channel_Id = "Familiar_Alerts"
        const val request_code = 100
        const val NotificationId = 888

        const val ACTION_DISMISS = "com.example.questlogalpha.handlers.action.DISMISS"
        const val ACTION_SNOOZE = "com.example.questlogalpha.handlers.action.SNOOZE"
        private val SNOOZE_TIME = TimeUnit.SECONDS.toMillis(5)

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: NotificationReceiver"
    }

}

//class BigTextIntentService : IntentService("BigTextIntentService") {
//
//    /**
//     * Handles action Dismiss in the provided background thread.
//     */
//    private fun handleActionDismiss() {
//        Log.d(TAG, "handleActionDismiss()")
//        val notificationManagerCompat =
//            NotificationManagerCompat.from(applicationContext)
//        notificationManagerCompat.cancel(MainActivity.NOTIFICATION_ID)
//    }
//
//    /**
//     * Handles action Snooze in the provided background thread.
//     */
//    private fun handleActionSnooze() {
//        Log.d(TAG, "handleActionSnooze()")
//        // You could use NotificationManager.getActiveNotifications() if you are targeting SDK 23
//// and above, but we are targeting devices with lower SDK API numbers, so we saved the
//// builder globally and get the notification back to recreate it later.
//        var notificationCompatBuilder: NotificationCompat.Builder =
//            GlobalNotificationBuilder.getNotificationCompatBuilderInstance()
//        // Recreate builder from persistent state if app process is killed
//        if (notificationCompatBuilder == null) { // Note: New builder set globally in the method
//            notificationCompatBuilder = recreateBuilderWithBigTextStyle()
//        }
//        val notification: Notification?
//        notification = notificationCompatBuilder.build()
//        if (notification != null) {
//            val notificationManagerCompat =
//                NotificationManagerCompat.from(applicationContext)
//            notificationManagerCompat.cancel(MainActivity.NOTIFICATION_ID)
//            try {
//                Thread.sleep(SNOOZE_TIME)
//            } catch (ex: InterruptedException) {
//                Thread.currentThread().interrupt()
//            }
//            notificationManagerCompat.notify(MainActivity.NOTIFICATION_ID, notification)
//        }
//    }
//
//    /*
//     * This recreates the notification from the persistent state in case the app process was killed.
//     * It is basically the same code for creating the Notification from MainActivity.
//     */
//    private fun recreateBuilderWithBigTextStyle(): NotificationCompat.Builder { // Main steps for building a BIG_TEXT_STYLE notification (for more detailed comments on
//// building this notification, check MainActivity.java)::
////      0. Get your data
////      1. Build the BIG_TEXT_STYLE
////      2. Set up main Intent for notification
////      3. Create additional Actions for the Notification
////      4. Build and issue the notification
//// 0. Get your data (everything unique per Notification).
//        val bigTextStyleReminderAppData: MockDatabase.BigTextStyleReminderAppData =
//            MockDatabase.getBigTextStyleData()
//        // 1. Retrieve Notification Channel for O and beyond devices (26+). We don't need to create
////    the NotificationChannel, since it was created the first time this Notification was
////    created.
//        val notificationChannelId: String = bigTextStyleReminderAppData.getChannelId()
//        // 2. Build the BIG_TEXT_STYLE.
//        val bigTextStyle =
//            NotificationCompat.BigTextStyle()
//                .bigText(bigTextStyleReminderAppData.getBigText())
//                .setBigContentTitle(bigTextStyleReminderAppData.getBigContentTitle())
//                .setSummaryText(bigTextStyleReminderAppData.getSummaryText())
//        // 3. Set up main Intent for notification
//        val notifyIntent = Intent(this, BigTextMainActivity::class.java)
//        notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val notifyPendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            notifyIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        // 4. Create additional Actions (Intents) for the Notification
//// Snooze Action
//        val snoozeIntent = Intent(this, BigTextIntentService::class.java)
//        snoozeIntent.action = ACTION_SNOOZE
//        val snoozePendingIntent = PendingIntent.getService(this, 0, snoozeIntent, 0)
//        val snoozeAction: NotificationCompat.Action =
//            NotificationCompat.Action.Builder(
//                R.drawable.ic_alarm_white_48dp,
//                "Snooze",
//                snoozePendingIntent
//            )
//                .build()
//        // Dismiss Action
//        val dismissIntent = Intent(this, BigTextIntentService::class.java)
//        dismissIntent.action = ACTION_DISMISS
//        val dismissPendingIntent =
//            PendingIntent.getService(this, 0, dismissIntent, 0)
//        val dismissAction: NotificationCompat.Action =
//            NotificationCompat.Action.Builder(
//                R.drawable.ic_cancel_white_48dp,
//                "Dismiss",
//                dismissPendingIntent
//            )
//                .build()
//        // 5. Build and issue the notification.
//// Notification Channel Id is ignored for Android pre O (26).
//        val notificationCompatBuilder =
//            NotificationCompat.Builder(
//                applicationContext, notificationChannelId
//            )
//        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder)
//        notificationCompatBuilder
//            .setStyle(bigTextStyle)
//            .setContentTitle(bigTextStyleReminderAppData.getContentTitle())
//            .setContentText(bigTextStyleReminderAppData.getContentText())
//            .setSmallIcon(R.drawable.ic_launcher)
//            .setLargeIcon(
//                BitmapFactory.decodeResource(
//                    resources,
//                    R.drawable.ic_alarm_white_48dp
//                )
//            )
//            .setContentIntent(notifyPendingIntent)
//            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
//            .setCategory(Notification.CATEGORY_REMINDER)
//            .setPriority(bigTextStyleReminderAppData.getPriority())
//            .setVisibility(bigTextStyleReminderAppData.getChannelLockscreenVisibility())
//            .addAction(snoozeAction)
//            .addAction(dismissAction)
//        return notificationCompatBuilder
//    }
//
//    companion object {
//        private const val TAG = "BigTextService"
//        const val ACTION_DISMISS =
//            "com.example.android.wearable.wear.wearnotifications.handlers.action.DISMISS"
//        const val ACTION_SNOOZE =
//            "com.example.android.wearable.wear.wearnotifications.handlers.action.SNOOZE"
//        private val SNOOZE_TIME = TimeUnit.SECONDS.toMillis(5)
//    }
//}
