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
import kotlin.coroutines.CoroutineContext

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

    /** Adds a new skill to the database.
     * @param skill The skill to add*/
    fun onAddNewSkill(skill: Skill) {
        viewModelScope.launch {
            insert(skill)
        }
    }

    // todo add delete skill public and private functions

    // --------- debug -------- //
    /** Adds a debug skill with the name "NewSkill", has 0 xp, and is of SkillType.NONE */
    fun onAddDebugSkill() {
        // todo launch to new screen -- ViewEditSkill
        viewModelScope.launch {
            insert(Skill("NewSkill", 0, SkillType.NONE))
        }
    }

    // --------- navigation -------- //
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

    // --------- add experience/check for level -------- //
    /** Adds experience to a skill, checks if it is high enough to level, and levels the skill if it is. Updates database.
     * @param skillId the String id for the Skill
     * @param experience the experience to award
     * */
    fun addExperience(skillId: String, experience: Double) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val skill = database.getSkillById(skillId) ?: return@withContext
                skill.currentXP += experience
                if (skill.canLevelUp) {
                    skill.onLevelUp()
                }
                database.updateSkill(skill)
            }
        }
    }

    // private functions
    // ---------------------------------------------------------------- //

    private suspend fun updateSkill(skill:Skill){
        withContext(Dispatchers.IO){
            database.updateSkill(skill)
        }
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