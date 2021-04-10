package com.example.questlogalpha.widget

import android.annotation.TargetApi
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.questlogalpha.MainActivity
import com.example.questlogalpha.R
import kotlin.math.ceil


class QuestLogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->

            Log.d(TAG, "appwidgetid: " + appWidgetId)

            // Create an Intent to launch MainActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            // intent to populate view list adapter
            val intent = Intent(context, QuestLogWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

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

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {


        // See the dimensions and
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)


        // Get min width and height.
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)


        // Obtain appropriate widget and update it.
        appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, minWidth, minHeight, appWidgetId))
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    /**
     * Determine appropriate view based on row or column provided.
     *
     * @param minWidth
     * @param minHeight
     * @param appWidgetId
     * @return
     */
    private fun getRemoteViews(context: Context, minWidth: Int, minHeight: Int, appWidgetId: Int): RemoteViews? {
        // First find out rows and columns based on width provided.
        val rows = getCellsForSize(minHeight)
        val columns = getCellsForSize(minWidth)

        // intent to populate view list adapter
        val intent = Intent(context, QuestLogWidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.putExtra(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, columns) //send column info to the list adapter, it will handle sizing from there

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        val views: RemoteViews = RemoteViews(
            context.packageName,
            R.layout.questlog_appwidget_info
        )

        // When intents are compared, the extras are ignored, so we need to embed the extras into the data so that the extras will not be ignored.
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

        // set the adapter
        views.setRemoteAdapter(R.id.quest_list_widget, intent)
        views.setEmptyView(R.id.quest_list_widget, R.id.quest_list_widget_empty)

        return views
    }

    /**
     * Returns number of cells needed for given [size] of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private fun getCellsForSize(size: Int): Int {
         return (ceil(size + 30.toDouble()) /70.toDouble()).toInt()
    }

    companion object {
        const val TAG: String = "KSLOG: QuestLogWidgetProvider"
    }
}