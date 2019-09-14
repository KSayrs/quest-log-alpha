package com.example.questlogalpha.quests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

//   val questsString = Transformations.map(allQuests) {quests ->
//     //  formatQuests(quests,application.resources)
//   }

    init {
        initializeQuest()
    }

    private fun initializeQuest() {
        uiScope.launch {
            val x = getAllQuests()
            newQuest.value = x?.get(0)
        }
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

    fun onQuestCreateFinished() {
        uiScope.launch {
            val quest = Quest("New Quest", "test description")
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
    
    // --------------- delete quest -------------------
    fun onDeleteQuest(questId: String) {
        uiScope.launch {
            deleteQuest(questId)
            newQuest.value = null
        }
    }

    private suspend fun deleteQuest(questId: String) {
         withContext(Dispatchers.IO) {
             database.deleteQuestById(questId)
         }
    }
    
    
 //  private val _items = MutableLiveData<List<Quest>>().apply { value = emptyList() }
 //  val items: LiveData<List<Quest>> = _items
}