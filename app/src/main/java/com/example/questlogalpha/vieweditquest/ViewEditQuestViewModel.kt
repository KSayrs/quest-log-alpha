package com.example.questlogalpha.vieweditquest

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.*
import com.example.questlogalpha.quests.Difficulty
import com.example.questlogalpha.quests.QuestsDao
import kotlinx.coroutines.*

/** ************************************************************************************************
 * [AndroidViewModel] for the [ViewEditQuestFragment] screen.
 * ********************************************************************************************** */
class ViewEditQuestViewModel (private val questId: String, val database: QuestsDao, val globalVariableData: GlobalVariablesDao, application: Application) : AndroidViewModel(application), AdapterView.OnItemSelectedListener {

    val currentQuest : Quest? get() = _currentQuest
    private var _currentQuest : Quest ?= null
    val app = application

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val difficulty = MutableLiveData<Difficulty>()
    val objectives = MutableLiveData<ArrayList<Objective>>()
    val modifiedObjective = MutableLiveData<Objective>()
    val modifiedReward = MutableLiveData<SkillReward>()
    val rewards = MutableLiveData<ArrayList<SkillReward>>()
    val storedNotifications = MutableLiveData<ArrayList<StoredNotification>>()

    // todo observe this and wait to show items until this is complete
    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private var isNewQuest: Boolean = true
    private var isDataLoaded = false
    private var notificationId:GlobalVariable? = null
    val skillRewards = arrayListOf<Skill>()

    var toast = Toast.makeText(getApplication(), "Item Selected", Toast.LENGTH_SHORT)

    // init happens after the fragment's onCreateView
    init {
        isNewQuest = (questId == "")
        Log.d(TAG, "init: isNewQuest: $isNewQuest")
        Log.d(TAG, "init: questId: $questId")

        if (isNewQuest) {
            title.value = ""
            description.value = ""
            difficulty.value = Difficulty.MEDIUM
            objectives.value = arrayListOf()
            modifiedObjective.value = null
            modifiedReward.value = null
            rewards.value = arrayListOf()
            storedNotifications.value = arrayListOf()
            Log.d(TAG, "init: creating new quest")
        }
        else
        {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val q =  async { database.getQuestById(questId) }
                    val loadedQuest = q.await()
                    async { onDataLoaded(loadedQuest) }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                notificationId = globalVariableData.getVariableWithName("NotificationId")
                Log.d(TAG, "global variable retrieved: $notificationId")
                // todo check for and clear out old notifications
            }
        }

    }

    private fun onDataLoaded(quest: Quest?)
    {
        if (quest == null) {
            Log.e(TAG, "onDataLoaded: existing quest: quest is null!")
            return
        }
        _currentQuest = quest

        title.postValue(currentQuest?.title)
        description.postValue(currentQuest?.description)
        difficulty.postValue(currentQuest?.difficulty)
        objectives.postValue(currentQuest?.objectives)
        modifiedObjective.postValue(null)
        modifiedReward.postValue(null)
        rewards.postValue(currentQuest?.rewards)
        storedNotifications.postValue(currentQuest?.notifications)

        isDataLoaded = true
        Log.d(TAG, "onDataLoaded complete")
    }

    // -------------------------------------------------------------------- //
    // ------------------------ database updates -------------------------- //
    fun onSaveQuest() : Boolean {
        return if(title.value != "") {
            viewModelScope.launch {
                if (isNewQuest) insert()
                else update()

                _navigateToQuestsViewModel.value = true // navigate back to the quests screen
            }
            true
        }
        else {
            Toast.makeText(getApplication(), "Title is empty!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun onDeleteQuest()
    {
        if (isNewQuest) return

        viewModelScope.launch {
            delete()
            _navigateToQuestsViewModel.value = true // navigate back to the quests screen
        }
    }

    private suspend fun update() {
        if(_currentQuest == null) {
            Log.e(TAG,"update called when quest is null!")
            Toast.makeText(getApplication(), "Something went wrong.", Toast.LENGTH_SHORT).show()
            return
        }
        withContext(Dispatchers.IO) {
            Log.d(TAG, "quest title: " + _currentQuest!!.title)
            Log.d(TAG, "Quest ID: " + currentQuest!!.id)
            Log.d(TAG, "quest description: " + currentQuest!!.description)
            Log.d(TAG, "quest rewards: " + currentQuest!!.rewards)
            Log.d(TAG, "quest notifications: " + currentQuest!!.notifications)

            _currentQuest!!.title = title.value.toString()
            _currentQuest!!.description = description.value.toString()
            _currentQuest!!.difficulty = difficulty.value!!
            _currentQuest!!.objectives = objectives.value!!
            _currentQuest!!.rewards = rewards.value!!
            _currentQuest!!.notifications = storedNotifications.value!!
            database.updateQuest(_currentQuest!!)
        }
    }

    private suspend fun insert() {
        withContext(Dispatchers.IO){
            val newQuest = Quest(
                title.value.toString(), 
                description.value.toString(),
                objectives = objectives.value!!,
                difficulty = difficulty.value!!,
                rewards = rewards.value!!,
                notifications = storedNotifications.value!!
            )
            database.insertQuest(newQuest)
            globalVariableData.updateVariable(notificationId!!)

            Log.d(TAG, "Newly made quest title: " + newQuest.title)
            Log.d(TAG, "Newly made quest description: " + newQuest.description)
            Log.d(TAG, "Newly made quest difficulty: " + newQuest.difficulty)
            Log.d(TAG, "Newly made quest id: " + newQuest.id)
            Log.d(TAG, "Newly made quest rewards: " + newQuest.rewards)
            Log.d(TAG, "Newly made quest notifications: " + newQuest.notifications)
            Log.d(TAG, "Quest made on: " + newQuest.dateCreated)
        }
    }

    private suspend fun delete() {
        if(_currentQuest == null) {
            Log.e(TAG,"delete called when quest is null!")
            Toast.makeText(getApplication(), "Something went wrong.", Toast.LENGTH_SHORT).show()
            return
        }

        withContext(Dispatchers.IO) {
            database.deleteQuestById(_currentQuest!!.id)
            Log.d(TAG, "Deleted quest with name: " + _currentQuest!!.title)
        }
    }

    // ---------------------------------------------------------- //
    // --------------------- on-click handlers ------------------ //

    // ---------------- spinner handlers ---------------- //
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        difficulty.value = parent.getItemAtPosition(pos) as Difficulty? //Difficulty.valueOf(parent.getItemAtPosition(pos).toString())

        toast.setText(parent.getItemAtPosition(pos).toString() + " selected")
        toast.show()
    }

    // todo figure out if can delete
    override fun onNothingSelected(parent: AdapterView<*>) {
        toast.setText("nothing selected")
        toast.show()
        // Another interface callback
        // do nothing?
    }

    // ---------------- objective handlers ---------------- //
    fun onObjectiveChecked(objective: Objective) {
        objective.completed = !objective.completed
        modifiedObjective.value = objective
    }

    fun onObjectiveDeleted(objective: Objective) {
        objectives.value?.remove(objective)
        Toast.makeText(getApplication(), "Objective removed", Toast.LENGTH_SHORT).show()
        modifiedObjective.value = null
    }

    fun onObjectiveEdit(objective: Objective, objectiveText: String) {
        objective.description = objectiveText
        modifiedObjective.value = objective
    }

    fun onAddObjective(){
        val newObjective = Objective()
        objectives.value!!.add(newObjective)
        modifiedObjective.value = newObjective
    }

    // ---------------- reward handlers ---------------- //
    fun onPositiveButtonClicked(dictionary: Map<String, Any>) {
        val amount:Double = dictionary["amount"] as Double
        val skill: Skill = dictionary["skill"] as Skill
        onAddReward(amount, skill)
    }

    private fun onAddReward(amount: Double, skill: Skill) {
        Log.d(TAG, "onAddReward(): ${skill.name}, $amount")
        val newReward = SkillReward(skill.id, amount, skill.name)
        rewards.value!!.add(newReward)
        modifiedReward.value = newReward
        skillRewards.add(skill)
    }

    fun onRemoveReward(reward: SkillReward) {
        rewards.value!!.remove(reward)

        var skillToRemove: Skill? = null
        for(item in skillRewards) {
            if(reward.id == item.id) {
                skillToRemove = item
                break
            }
        }
        if(skillToRemove != null) {
            skillRewards.remove(skillToRemove)
            modifiedReward.value = null
        }
    }

    // ------------------- notifications -------------------- //

    fun onAddStoredNotification(storedNotification: StoredNotification){
        Log.d(TAG,"onAddStoredNotification: $storedNotification")
        storedNotifications.value!!.add(storedNotification)
        incrementId()
    }

    fun onRemoveStoredNotification(storedNotification: StoredNotification){
        Log.d(TAG,"onAddStoredNotification: $storedNotification")
        storedNotifications.value!!.remove(storedNotification)
        decrementId()
    }

    fun getNextNotificationId() : Int
    {
        incrementId()
        return notificationId!!.value
    }

    fun getNotificationId() : Int
    {
        return notificationId!!.value
    }

    private fun incrementId() {
        notificationId!!.value++
    }

    private fun decrementId() {
        notificationId!!.value--
    }

    // ------------------------ navigation ----------------------- //
    private val _navigateToQuestsViewModel = MutableLiveData<Boolean>()
    val navigateToQuestsViewModel: LiveData<Boolean> get() = _navigateToQuestsViewModel

    fun doneNavigating() {
        Log.d(TAG,"doneNavigating() called")
        _navigateToQuestsViewModel.value = false
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        var TAG: String = "KSLOG: ViewEditQuestViewModel"
    }
}
