package com.example.questlogalpha.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.example.questlogalpha.MainActivity
import com.example.questlogalpha.R


class QuestLogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->

            Log.d(TAG, "appwidgetid: " + appWidgetId)

            // Create an Intent to launch MainActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    PendingIntent.getActivity(context, 0, intent,0)
                }

            // intent to populate view list adapter
            val intent = Intent(context, QuestLogWidgetService::class.java)
            intent.putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId
            )

            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME));

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.questlog_appwidget_info
            ).apply {
                setOnClickPendingIntent(R.id.quest_widget_item_layout, pendingIntent)
            }

            // set the adapter
            views.setRemoteAdapter(R.id.quest_list_widget, intent)
            views.setEmptyView(R.id.quest_list_widget, R.id.quest_list_widget_empty)


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        const val TAG: String = "KSLOG: QuestLogWidgetProvider"
    }
}