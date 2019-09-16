package com.example.questlogalpha.vieweditquest

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.quests.QuestsDao
import kotlinx.coroutines.*

class ViewEditQuestViewModel (private val questId: String, val database: QuestsDao, application: Application) : AndroidViewModel(application), AdapterView.OnItemSelectedListener {

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

 //   private var questId: String? = null
    private var isNewTask: Boolean = false

    var toast = Toast.makeText(getApplication(), "Item Selected", Toast.LENGTH_LONG)

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // ------------------ navigation -----------------

  // private val _navigateToQuestsViewModel = MutableLiveData<Boolean?>()
  // val navigateToQuestsViewModel: LiveData<Boolean?> get() = _navigateToQuestsViewModel

  // fun doneNavigating() {
  //     _navigateToQuestsViewModel.value = null
  // }

    // ----------- on-click handlers --------------

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
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
}