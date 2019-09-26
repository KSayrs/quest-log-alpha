package com.example.questlogalpha.quests

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.Quest
import kotlinx.coroutines.*

// if you'll need application stuff, change this to AndroidViewModel
class QuestsViewModel (val database: QuestsDao) : ViewModel() {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
//    private var selectedQuest = MutableLiveData<Quest?>()

    private val _navigateToViewEditQuest = MutableLiveData<String?>()

    // todo should this be called in a coroutine?
    val quests = database.getAllQuests()

    /**
     * When true immediately navigate back to the [ViewEditQuestFragment]
     */
    val navigateToViewEditQuest: LiveData<String?>
        get() = _navigateToViewEditQuest

    /**
     * Call this immediately after navigating to [ViewEditQuestFragment]
     */
    fun doneNavigating() {
        _navigateToViewEditQuest.value = null
    }

    init {
        Log.d("$TAG QusetsVM","QuestsViewModel initiated")
       // initializeQuest()
    }

    private suspend fun getAllQuests(): List<Quest>? {
        return withContext(Dispatchers.IO){
            database.getAllQuests().value
        }
    }

    private suspend fun getQuestFromDatabase(questId: String): Quest? {
        return withContext(Dispatchers.IO){
            database.getQuestById(questId)
        }
    }

    fun onQuestCreate() {
        uiScope.launch {
            Log.d("$TAG onQuestCreate", "Logging")
            _navigateToViewEditQuest.value = "" // todo change this to something more comprehensive, like null. Or make it a string instead ofa quest.
        }
    }

    fun onQuestEdit(quest: Quest) {
        uiScope.launch {
            Log.d("$TAG onQuestEdit", "Logging")
            _navigateToViewEditQuest.value = quest.id
        }
    }

    // long running work to leave the UI thread free to handle other things
    private suspend fun insert(quest: Quest){
        withContext(Dispatchers.IO){
            database.insertQuest(quest)
        }
    }

    fun onQuestUpdateFinished(quest: Quest) {
        uiScope.launch {
            update(quest)
        }
    }

    private suspend fun update(quest: Quest) {
        withContext(Dispatchers.IO) {
            database.updateQuest(quest)
        }
    }

    // ----------------------- delete quest ---------------------------- //
    fun onDeleteQuest(questId: String) {
        uiScope.launch {
            deleteQuest(questId)
        }
    }

    private suspend fun deleteQuest(questId: String) {
         withContext(Dispatchers.IO) {
             database.deleteQuestById(questId)
         }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsViewModel"
    }
}
