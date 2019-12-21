package com.example.questlogalpha.skills

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.data.SkillType
import com.example.questlogalpha.personnage.SkillsDao
import kotlinx.coroutines.*

class SkillsViewModel (val database: SkillsDao) : ViewModel() {

    val skills = database.getAllSkills()

    /**
     * TODO add navigation
     */
    private val _navigateToViewEditSkill = MutableLiveData<String?>()
    val navigateToViewEditSkill: LiveData<String?> get() = _navigateToViewEditSkill

 // /**
 //  * Call this immediately after navigating to [ViewEditSkillFragment]
 //  */
    fun doneNavigating() {
       _navigateToViewEditSkill.value = null
    }

    init {
        Log.d(TAG," initiated")
    }

    // public functions
    // ---------------------------------------------------------------- //

    fun onAddDebugSkill()
    {
        // todo launch to new screen -- ViewEditSkill
        viewModelScope.launch {
            insert(Skill("NewSkill", 0, SkillType.NONE))
        }
    }

    fun onAddSkill() {
        viewModelScope.launch {
            Log.d("$TAG onAddSkill", "Logging")
            _navigateToViewEditSkill.value = ""
        }
    }

    fun onEditSkill(skill: Skill) {
        viewModelScope.launch {
            Log.d("$TAG onEditSkill", "Logging")
            _navigateToViewEditSkill.value = skill.id
        }
    }


    // private functions
    // ---------------------------------------------------------------- //

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