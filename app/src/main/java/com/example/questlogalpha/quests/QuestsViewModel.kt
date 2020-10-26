package com.example.questlogalpha.quests

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.GlobalVariablesDao
import com.example.questlogalpha.data.Icon
import com.example.questlogalpha.data.IconsDao
import com.example.questlogalpha.data.Quest
import kotlinx.coroutines.*

// if you'll need application stuff, change this to inherit from AndroidViewModel
/** ************************************************************************************************
 * [ViewModel] for the list of quests.
 * ********************************************************************************************** */
@Suppress("DeferredResultUnused")
class QuestsViewModel (val database: QuestsDao, val iconDatabase: IconsDao, val globalVariables: GlobalVariablesDao) : ViewModel() {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToViewEditQuest = MutableLiveData<String?>()

    var viewingCompleted = MutableLiveData<Boolean>(false)
        private set

    var quests = database.getAllQuests()

    val currentFamiliar = MutableLiveData<Int>()

    val icons = iconDatabase.getAllIcons()

    /** When true immediately navigate back to the [com.example.questlogalpha.vieweditquest.ViewEditQuestFragment] */
    val navigateToViewEditQuest: LiveData<String?>
        get() = _navigateToViewEditQuest

    /** Call this immediately after navigating to [com.example.questlogalpha.vieweditquest.ViewEditQuestFragment] */
    fun doneNavigating() {
        _navigateToViewEditQuest.value = null
    }

    init {
        Log.d(TAG,"QuestsViewModel initiated")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val familiarIcon =  async { globalVariables.getVariableWithName("CurrentFamiliar")!!.value }

                val loadedFamiliar = familiarIcon.await()
                async { currentFamiliar.postValue(loadedFamiliar) }
            }
        }
    }

    // ---------------------- create/edit quest (navigation) -------------------------- //

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

    // ---------------------- search for icon -------------------------- //
    fun onIconSearch(desc: String) : LiveData<List<Icon>> {
        return searchIcons(desc)
    }

    @WorkerThread
    fun searchIcons(desc : String) : LiveData<List<Icon>>{
        return iconDatabase.searchIcons(desc)
    }

    // ----------------------- set quest icon -------------------------- //
    fun onSetQuestIcon(questId: String, iconResourceId: Int) {
        uiScope.launch {
            Log.d("$TAG onSetQuestIcon", "iconResourceId: $iconResourceId")
            setQuestIcon(questId, iconResourceId)
        }
    }

    private suspend fun setQuestIcon(questId: String, iconResourceId: Int) {
        withContext(Dispatchers.IO) {
            database.updateIcon(questId, iconResourceId)
        }
    }

    // ----------------------- complete quest -------------------------- //
    fun onSetQuestCompletion(questId: String, completed: Boolean) {
        uiScope.launch {
            Log.d("$TAG onCompleteQuest", "Completed: $completed")
            setQuestCompletion(questId, completed)
        }
    }

    private suspend fun setQuestCompletion(questId: String, completed: Boolean) {
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
