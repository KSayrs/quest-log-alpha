package com.example.questlogalpha.skills

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.personnage.SkillsDao
import kotlinx.coroutines.*

class SkillsViewModel (val database: SkillsDao) : ViewModel() {

    val skills = database.getAllSkills()

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