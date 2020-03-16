/*
* View Model for the list of quests
*/
package com.example.questlogalpha.quests

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
        Log.d(TAG,"QuestsViewModel initiated")
    }

    fun onQuestCreate() {
        uiScope.launch {
            _navigateToViewEditQuest.value = ""
        }
    }

    fun onQuestEdit(quest: Quest) {
        uiScope.launch {
            _navigateToViewEditQuest.value = quest.id
        }
    }

    // ----------------------- complete quest -------------------------- //
    fun onSetQuestCompletion(questId: String, completed: Boolean){
        uiScope.launch {
            Log.d("$TAG onCompleteQuest", "Completed: $completed")
            setQuestCompletion(questId, completed)
        }
    }

    private suspend fun setQuestCompletion(questId: String, completed: Boolean)
    {
        withContext(Dispatchers.IO) {
            database.updateCompleted(questId, completed)
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

    // ----------------------- update quest ---------------------------- //
    /** For use outside the normal quest screens. Updates a [quest] in the database. */
    fun onUpdateQuest(quest: Quest) {
        uiScope.launch {
            updateQuest(quest)
        }
    }

    private suspend fun updateQuest(quest: Quest) {
        withContext(Dispatchers.IO) {
            database.updateQuest(quest)
        }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsViewModel"
    }
}
