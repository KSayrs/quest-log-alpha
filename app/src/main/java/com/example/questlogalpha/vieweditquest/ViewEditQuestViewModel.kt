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
import com.example.questlogalpha.ITalkToDialogs
import com.example.questlogalpha.data.*
import com.example.questlogalpha.quests.Difficulty
import com.example.questlogalpha.quests.QuestsDao
import kotlinx.coroutines.*

class ViewEditQuestViewModel (private val questId: String, val database: QuestsDao, application: Application) : AndroidViewModel(application), AdapterView.OnItemSelectedListener, ITalkToDialogs {

    val currentQuest : Quest? get() = _currentQuest
    private var _currentQuest : Quest ?= null

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val difficulty = MutableLiveData<Difficulty>()
    val objectives = MutableLiveData<ArrayList<Objective>>() // not observed
    val modifiedObjective = MutableLiveData<Objective>()
    val modifiedReward = MutableLiveData<SkillReward>()
    val rewards = MutableLiveData<ArrayList<SkillReward>>()

    private var isNewQuest: Boolean = true
    private var isDataLoaded = false
    val skillRewards = arrayListOf<Skill>()

    var toast = Toast.makeText(getApplication(), "Item Selected", Toast.LENGTH_SHORT)

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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
            objectives.value!!.add(Objective("New Objective", false))
            modifiedObjective.value = null
            modifiedReward.value = null
            rewards.value = arrayListOf()
            Log.d(TAG, "init: creating new quest")
        }
        else
        {
            // apparently this can replace the whole job stuff
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val q: Quest? = database.getQuestById(questId) ?: return@withContext

                    if (q == null) Log.e(TAG, "init: existing quest: quest is null!")
                    _currentQuest = q
                    isDataLoaded = true
                }

                // todo figure out a way to wait until data is loaded
                title.value = currentQuest?.title
                description.value = currentQuest?.description
                difficulty.value = currentQuest?.difficulty
                objectives.value = currentQuest?.objectives
                modifiedObjective.value = null
                modifiedReward.value = null
                rewards.value = currentQuest?.rewards
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------ database updates -------------------------- //
    fun onSaveQuest()
    {
        viewModelScope.launch {
            if(title.value != "") {
                if (isNewQuest) insert()
                else update()

                _navigateToQuestsViewModel.value = true // navigate back to the quests screen
            }
            else Toast.makeText(getApplication(), "Title is empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun update() {
        withContext(Dispatchers.IO) {
            if(_currentQuest == null) {
                Log.e(TAG,"update called when quest is null!")
                Toast.makeText(getApplication(), "Something went wrong.", Toast.LENGTH_SHORT).show()
                return@withContext
            }

            Log.d(TAG, "quest title: " + _currentQuest!!.title)
            Log.d(TAG, "Quest ID: " + currentQuest!!.id)
            Log.d(TAG, "quest description: " + currentQuest!!.description)
            Log.d(TAG, "quest rewards: " + currentQuest!!.rewards)

            _currentQuest!!.title = title.value.toString()
            _currentQuest!!.description = description.value.toString()
            _currentQuest!!.difficulty = difficulty.value!!
            _currentQuest!!.objectives = objectives.value!!
            _currentQuest!!.rewards = rewards.value!!
            database.updateQuest(_currentQuest!!)
        }
    }

    private suspend fun insert(){
        withContext(Dispatchers.IO){
            val newQuest = Quest(title.value.toString(), description.value.toString(), objectives = objectives.value!!, difficulty = difficulty.value!!, rewards = rewards.value!!)
            database.insertQuest(newQuest)

            Log.d(TAG, "Newly made quest title: " + newQuest.title)
            Log.d(TAG, "Newly made quest description: " + newQuest.description)
            Log.d(TAG, "Newly made quest difficulty: " + newQuest.difficulty)
            Log.d(TAG, "Newly made quest id: " + newQuest.id)
            Log.d(TAG, "Quest made on: " + newQuest.dateCreated)
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
    fun onObjectiveChecked(objectiveId: String) {
        for(obj in objectives.value!!) {
            if(obj.id == objectiveId){
                obj.completed = !obj.completed
                modifiedObjective.value = obj
                return
            }
        }

        Log.e(TAG, "onObjectiveChecked: objective '${objectiveId}' not found for quest ${currentQuest?.title}!")
    }

    fun onObjectiveDeleted(objective: Objective) {
        objectives.value?.remove(objective)
        Toast.makeText(getApplication(), "Objective removed", Toast.LENGTH_SHORT).show()
        modifiedObjective.value = null
    }

    // todo figure out what this comment means vvv
    // todo if this works we can edit onObjectiveChecked
    fun onObjectiveEdit(objective: Objective, objectiveText: String) {
        Log.d(TAG, "onObjectiveEdit()")
        objective.description = objectiveText
        modifiedObjective.value = objective
    }

    fun onAddObjective(){
        Log.d(TAG, "onAddObjective()")
        val newObjective = Objective()
        objectives.value!!.add(newObjective)
        modifiedObjective.value = newObjective
    }

    // ---------------- reward handlers ---------------- //
    override fun onPositiveButtonClicked(dictionary: Map<String, Any>) {
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

    // ----------------------------------------------------------- //
    // ------------------------ navigation ----------------------- //
    private val _navigateToQuestsViewModel = MutableLiveData<Boolean>()
    val navigateToQuestsViewModel: LiveData<Boolean> get() = _navigateToQuestsViewModel

    fun doneNavigating() {
        Log.d(TAG,"doneNavigating() called")
        _navigateToQuestsViewModel.value = false
    }

    // ------------------------------------------------------------------ //
    // -------------------------- log util ------------------------------ //
    companion object {
        var TAG: String = "KSLOG: ViewEditQuestViewModel"
    }
}
