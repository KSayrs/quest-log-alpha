package com.example.questlogalpha.quests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.questlogalpha.data.Quest
import kotlinx.coroutines.*

// AndroidViewModel offers application stuff like toasts and cameras. IF this class doesn't need those things, change
// this to ViewModel.
class QuestsViewModel (val database: QuestsDao, application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var newQuest = MutableLiveData<Quest?>()
    private val allQuests = database.getAllQuests()

    init {
    //    initializeQuest()
    }

    private fun initializeQuest(questId: String) {
        uiScope.launch {
            newQuest.value = getQuestFromDatabase(questId)
        }
    }

    private suspend fun getQuestFromDatabase(questId: String): Quest? {
        return withContext(Dispatchers.IO){
            database.getQuestById(questId)
        }
    }

    fun onQuestCreateFinished() {
        uiScope.launch {
            val quest = Quest("")
            insert(quest)
            newQuest.value = getQuestFromDatabase(quest.id)
        }
    }

    // long running work to leave the UI thread free to handle other things
    private suspend fun insert(quest: Quest){
        withContext(Dispatchers.IO){
            database.insertQuest(quest)
        }
    }
 //  private val _items = MutableLiveData<List<Quest>>().apply { value = emptyList() }
 //  val items: LiveData<List<Quest>> = _items
}