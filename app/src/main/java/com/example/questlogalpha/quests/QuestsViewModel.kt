package com.example.questlogalpha.quests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.questlogalpha.data.QuestLogDatabase

// AndroidViewModel offers application stuff like toasts and cameras. IF this class doesn't need those things, change
// this to ViewModel.
class QuestsViewModel (val dataSource: QuestsDao, application: Application) : AndroidViewModel(application) {


}