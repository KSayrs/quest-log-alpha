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
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.quests.Difficulty
import com.example.questlogalpha.quests.QuestsDao
import kotlinx.coroutines.*

class ViewEditQuestViewModel (private val questId: String, val database: QuestsDao, application: Application) : AndroidViewModel(application), AdapterView.OnItemSelectedListener {

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    private val difficulty = MutableLiveData<Difficulty>()

    private var isNewQuest: Boolean = (questId != "")

    var toast = Toast.makeText(getApplication(), "Item Selected", Toast.LENGTH_SHORT)

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // todo is init correct to use?
    // todo invert bool
    init {
        if (!isNewQuest) {
            // todo apparently this can replace the whole job stuff
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val q: Quest? = database.getQuestById(questId) ?: return@withContext
                    title.value = q!!.title
                    description.value = q!!.description
                    difficulty.value = q!!.difficulty
                }
            }
        }
        else
        {
            title.value = ""
            description.value = "..."
            difficulty.value = Difficulty.MEDIUM
        }
    }

    // ------------------------ database updates -------------------------- //
    fun onSaveQuest()
    {
        viewModelScope.launch {
            if(isNewQuest) insert()
            else update()

            // todo this doesn't work
            _navigateToQuestsViewModel.value = true // navigate back to the quests screen
        }
    }

    private suspend fun update() {
        withContext(Dispatchers.IO) {
            val currentQuest:Quest? = database.getQuestById(questId) ?: return@withContext //todo learn what this means
            if(currentQuest == null) {
                Log.e(TAG,"update called when quest is null!")
            }

            currentQuest!!.title = title.value.toString()
            Log.d(TAG, "quest title: " + currentQuest.title)
            currentQuest.description = title.value.toString()
            Log.d(TAG, "quest title: " + currentQuest.description)
            currentQuest.difficulty = difficulty.value!!
            Log.d(TAG, "Quest ID: " + currentQuest.id)
            database.updateQuest(currentQuest)
        }
    }

    private suspend fun insert(){
        withContext(Dispatchers.IO){
            val newQuest = Quest(title.value.toString(),description.value.toString(), difficulty = difficulty.value!!)
            database.insertQuest(newQuest)

            Log.d(TAG, "Newly made quest title: " + newQuest.title)
            Log.d(TAG, "Newly made quest description: " + newQuest.description)
            Log.d(TAG, "Newly made quest difficulty: " + newQuest.difficulty)
            Log.d(TAG, "Newly made quest id: " + newQuest.id)
        }
    }

    // ----------- on-click handlers --------------

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

        difficulty.value = parent.getItemAtPosition(pos) as Difficulty? //Difficulty.valueOf(parent.getItemAtPosition(pos).toString())

        toast.setText(parent.getItemAtPosition(pos).toString() + " selected")
        toast.show()

        //  // An item was selected. You can retrieve the selected item using
        //  // parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        toast.setText("nothing selected")
        toast.show()
        // Another interface callback
        // do nothing?
    }


    // ------------------ navigation -----------------

    private val _navigateToQuestsViewModel = MutableLiveData<Boolean>()
    val navigateToQuestsViewModel: LiveData<Boolean> get() = _navigateToQuestsViewModel

    fun doneNavigating() {
        Log.d(TAG,"doneNavigating() called")
        _navigateToQuestsViewModel.value = false
    }


    // -------------------------- log util ------------------------------ //

    companion object {
        const val TAG: String = "KSLOG: ViewEditQuestViewModel"
    }
}