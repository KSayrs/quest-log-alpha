package com.example.questlogalpha.skills

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.personnage.SkillsDao
import kotlinx.coroutines.*

class SkillsViewModel (val database: SkillsDao) : ViewModel() {

    val skills = database.getAllSkills()

    /**
     * TODO add navigation
     */
 // private val _navigateToViewEditQuest = MutableLiveData<String?>()
 // val navigateToViewEditQuest: LiveData<String?> get() = _navigateToViewEditQuest

 // /**
 //  * Call this immediately after navigating to [ViewEditQuestFragment]
 //  */
    fun doneNavigating() {
  //     _navigateToViewEditQuest.value = null
    }

    init {
        Log.d(TAG," initiated")
    }

    private suspend fun getAllSkills(): List<Skill>? {
        return withContext(Dispatchers.IO){
            database.getAllSkills().value
        }
    }

    private suspend fun insert(skill: Skill){
        withContext(Dispatchers.IO){
            database.insertSkill(skill)
        }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: SkillsViewModel"
    }
}