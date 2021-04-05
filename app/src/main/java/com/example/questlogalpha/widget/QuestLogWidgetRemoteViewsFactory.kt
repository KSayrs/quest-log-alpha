package com.example.questlogalpha.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.example.questlogalpha.R
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.quests.QuestsDao


class QuestLogWidgetRemoteViewsFactory(context: Context?, intent: Intent) : RemoteViewsFactory  {
    private var context: Context? = null
    private val appWidgetId: Int
    private var quests: List<Quest>? = null
    private var questsDao: QuestsDao? = null
    private var database: QuestLogDatabase? = null

    init {

        Log.d(TAG, "init : context: $context")
        this.context = context
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )



        Log.d(TAG, "init : questsDao: $questsDao")
        //updateWidgetListView()
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
     //   updateWidgetListView()
        database = QuestLogDatabase.getInstance(context!!)
        questsDao = database!!.questLogDatabaseDao
    }

    override fun onDestroy() {
        // no-op
    }

    override fun getCount(): Int {
        val count =  if(quests != null) quests!!.size
        else 0

        Log.d(TAG, "getCount(): $count")
        return count
    }

    override fun getViewAt(position: Int): RemoteViews {
        Log.d(TAG, "getViewAt: WidgetCreatingView")
        val remoteView = RemoteViews(
            context!!.packageName,
            R.layout.quest_widget_item_view
        )
        Log.d(TAG, "Loading: getViewAt: " + quests?.get(position)?.title)
        remoteView.setTextViewText(R.id.quest_widget_item_title, quests?.get(position)?.title)

        return remoteView
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        quests = database!!.questLogDatabaseDao.getAllQuestsForWidget()
        updateWidgetListView()
    }

    // ---------------------------------------------------

    private fun updateWidgetListView() {
        Log.d(TAG, "quests: $quests")
    }

    companion object {
        const val TAG: String = "KSLOG: QuestLogWidgetRemoteViewsFactory"
    }
}