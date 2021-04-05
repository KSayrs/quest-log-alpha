package com.example.questlogalpha.widget

import android.content.Intent
import android.widget.RemoteViewsService


class QuestLogWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return QuestLogWidgetRemoteViewsFactory(this.applicationContext, intent)
    }
}