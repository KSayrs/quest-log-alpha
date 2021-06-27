package com.example.questlogalpha.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.example.questlogalpha.data.GlobalVariable
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.data.QuestLogDatabase
import kotlinx.coroutines.*

class BootBroadcastReceiver : BroadcastReceiver() {

    /** Currently triggers when the device boots */
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "onReceive")
        if(context == null) {
            Log.e(TAG, "context is null. Aborting operation for intent: $intent")
            return
        }

        NotifyAsyncTask().execute(context)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        var TAG: String = "KSLOG: BootBroadcastReceiver"
    }
}

private class NotifyAsyncTask : AsyncTask<Context?, Void?, Void?>() {

    override fun doInBackground(vararg params: Context?): Void? {
        Log.i(TAG, "Notify async started")
        val context = params[0]!!
        val dataSource = QuestLogDatabase.getInstance(context).questLogDatabaseDao
        val quests = dataSource.getAllQuestsForWidget()

        for (quest in quests) {
            for (storedNotification in quest.notifications) {

                val pendingIntent = NotificationUtil.scheduleNotification(
                    NotificationUtil.getNotification(
                        storedNotification,
                        quest.id,
                        context
                    ),
                    storedNotification.notificationTime,
                    storedNotification.id,
                    quest.id,
                    context
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                NotificationUtil.setAlarm(alarmManager, AlarmData(pendingIntent, storedNotification.notificationTime))

                Log.d(BootBroadcastReceiver.TAG, "quest notification reset: $storedNotification")
            }
        }
        return null
    }

    override fun onPostExecute(result: Void?) {

    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        var TAG: String = "KSLOG: NotifyAsyncTask"
    }
}
