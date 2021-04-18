package com.example.questlogalpha.quests

import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import com.example.questlogalpha.R
import com.example.questlogalpha.data.*
import com.example.questlogalpha.notifications.AlarmData
import com.example.questlogalpha.notifications.NotificationUtil
import kotlinx.coroutines.*

// if you'll need application stuff, change this to inherit from AndroidViewModel
/** ************************************************************************************************
 * [ViewModel] for the list of quests.
 * ********************************************************************************************** */
@Suppress("DeferredResultUnused")
class QuestsViewModel(application: Application,
                      val database: QuestsDao,
                      val iconDatabase: IconsDao,
                      val globalVariables: GlobalVariablesDao,
                      val familiarDatabase: FamiliarsDao) : AndroidViewModel(application), AdapterView.OnItemSelectedListener {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToViewEditQuest = MutableLiveData<String?>()

    private val context = getApplication<Application>().applicationContext

    var viewingCompleted = MutableLiveData<Boolean>(false)
        private set

    var quests = database.getAllQuests()
    var questsFromFragment = listOf<Quest>()

    val familiarImages = familiarDatabase.getAllFamiliarImages()

    private val currentFamiliar = MutableLiveData<GlobalVariable>()
    private val currentFamiliarOrdinal = MutableLiveData<Int>(0)

    // ...because this is what we'll want to expose
    val familiarLoaded = MediatorLiveData<Boolean>()

    val icons = iconDatabase.getAllIcons()

    /** When true immediately navigate back to the [com.example.questlogalpha.vieweditquest.ViewEditQuestFragment] */
    val navigateToViewEditQuest: LiveData<String?>
        get() = _navigateToViewEditQuest

    /** Call this immediately after navigating to [com.example.questlogalpha.vieweditquest.ViewEditQuestFragment] */
    fun doneNavigating() {
        _navigateToViewEditQuest.value = null
    }

    init {
        Log.d(TAG, "QuestsViewModel initiated")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // this loads before the database does apparently
                val allvars  = async { (globalVariables.getAllVariables()) }.await()
                if(!allvars.value.isNullOrEmpty()) {
                    val familiarIcon = async { globalVariables.getVariableWithName("CurrentFamiliar")!! }

                    val loadedFamiliar = familiarIcon.await()
                    async { currentFamiliar.postValue(loadedFamiliar) }
                }
                else {
                    currentFamiliar.postValue(GlobalVariable("CurrentFamiliar", R.drawable.ic_familiar_cat))
                }
            }
        }

        // merge the familiar loading data
        familiarLoaded.addSource(familiarImages) { result ->
            result?.let { familiarLoaded.value = result.isNotEmpty() && currentFamiliar.value != null }
        }
        familiarLoaded.addSource(currentFamiliar) { result ->
            result?.let {
                familiarLoaded.value = !familiarImages.value.isNullOrEmpty()
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
    fun searchIcons(desc: String) : LiveData<List<Icon>>{
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

    // -------------------------- familiar selection ------------------------------ //
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position) as Int

        if(currentFamiliar.value!!.value == item) return // don't update anything if it's the same

        currentFamiliar.value!!.value = item // .value.value is kind of fun
        currentFamiliarOrdinal.value = position


        // update alarms with new familiar icon
        uiScope.launch {
            withContext(Dispatchers.IO) {

                val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (questsFromFragment.isNotEmpty()) {
                    Log.d(TAG, "iterate through quests")

                    val alarms = arrayListOf<AlarmData>()
                    for (quest in questsFromFragment) {

                        Log.d(TAG, "iterating")
                        if (quest.notifications.isNotEmpty()) {

                            Log.d(TAG, "iterating has notifications")
                            for (notification in quest.notifications) {
                                notification.bigIcon = currentFamiliar.value!!.value
                                val pendingIntent = NotificationUtil.scheduleNotification(
                                    NotificationUtil.getNotification(
                                        notification,
                                        quest.id,
                                        context
                                    ),
                                    notification.notificationTime,
                                    notification.id,
                                    quest.id,
                                    context
                                )

                                alarms.add(AlarmData(pendingIntent, notification.notificationTime))
                            }
                        }
                        for (alarm in alarms) NotificationUtil.setAlarm(alarmManager, alarm)
                    }
                }
            }
        }

        // update database
        uiScope.launch {
            withContext(Dispatchers.IO) {
                globalVariables.updateVariable(currentFamiliar.value!!)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.e(TAG, "No familiar selected!")
    }

    fun getCurrentFamiliarOrdinal(adapter: FamiliarSpinnerAdapter) : Int {
        for (i in 0 until adapter.count) {
            val familiar: Int = adapter.getItem(i) as Int
            if(familiar == currentFamiliar.value!!.value) {
                return i
            }
        }
        Log.e(TAG, "current familiar not found in adapter list!!")
        return 0
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsViewModel"
    }
}
