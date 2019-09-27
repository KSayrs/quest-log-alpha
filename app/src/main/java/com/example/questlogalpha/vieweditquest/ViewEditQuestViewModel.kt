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

    val currentQuest : Quest? get() = _currentQuest
    private var _currentQuest : Quest ?= null

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val difficulty = MutableLiveData<Difficulty>()

    private var isNewQuest: Boolean = true

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
            Log.d(TAG, "init: creating new quest")
        }
        else
        {
            // apparently this can replace the whole job stuff
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val q: Quest? = database.getQuestById(questId) ?: return@withContext

                    if(q == null) Log.e(TAG, "init: existing quest: quest is null!")
                    _currentQuest = q
                }

                // todo figure out a way to wait until data is loaded
                title.value = currentQuest?.title
                description.value = currentQuest?.description
                difficulty.value = currentQuest?.difficulty
            }
        }
    }

    // ------------------------ database updates -------------------------- //
    fun onSaveQuest()
    {
        viewModelScope.launch {
            if(title.value != "") {
                if (isNewQuest) insert()
                else update()

                _navigateToQuestsViewModel.value = true // navigate back to the quests screen
            }
            else
            {
                Toast.makeText(getApplication(), "Title is empty!", Toast.LENGTH_SHORT).show()
            }
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
            Log.d(TAG, "quest title: " + currentQuest!!.description)

            _currentQuest!!.title = title.value.toString()
            _currentQuest!!.description = title.value.toString()
            _currentQuest!!.difficulty = difficulty.value!!
            database.updateQuest(_currentQuest!!)
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
            Log.d(TAG, "Quest made on: " + newQuest.dateCreated)
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
