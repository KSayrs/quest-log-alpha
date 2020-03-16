package com.example.questlogalpha.skills

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.data.SkillType
import com.example.questlogalpha.personnage.SkillsDao
import kotlinx.coroutines.*

/** ************************************************************************************************
 * View model for all skill-related screens:
 * - [SkillsFragment]
 * - [AddEditSkillDialogFragment]
 * - [com.example.questlogalpha.quests.AddRewardDialogFragment]
 * ********************************************************************************************** */
class SkillsViewModel (val database: SkillsDao) : ViewModel() {

    val skills = database.getAllSkills()

    init {
        Log.d(TAG,"initiated")
    }

    // public functions
    // ---------------------------------------------------------------- //

    /** Adds a new [skill] to the database. */
    fun onAddNewSkill(skill: Skill) {
        viewModelScope.launch {
            insert(skill)
        }
    }

    /** Edits a [skill] in the database. */
    fun onEditSkill(skill: Skill) {
        viewModelScope.launch {
            updateSkill(skill)
        }
    }

    /** Removes a [skill] from the database. */
    fun onDeleteSkill(skill: Skill) {
        viewModelScope.launch {
            delete(skill)
        }
    }

    /** Adds [experience] to a skill with [skillId], checks if it is high enough to level, and levels the skill if it is. Updates database. */
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

    /** Removes [experience] from a skill with [skillId] and makes a level adjustment if necessary. Updates database. */
    fun removeExperience(skillId: String, experience: Double) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val skill = database.getSkillById(skillId) ?: return@withContext
                skill.currentXP -= experience
                skill.onUndoExperienceChange()

                database.updateSkill(skill)
            }
        }
    }

    // --------- debug -------- //
    /** Adds a debug skill with the name "NewSkill", has 0 xp, and is of SkillType.NONE */
    fun onAddDebugSkill() {
        viewModelScope.launch {
            insert(Skill("NewSkill", 0, SkillType.NONE))
        }
    }

    // private functions
    // ---------------------------------------------------------------- //

    private suspend fun updateSkill(skill:Skill) {
        withContext(Dispatchers.IO) {
            database.updateSkill(skill)
        }
    }

    private suspend fun getAllSkills(): List<Skill>? {
        return withContext(Dispatchers.IO) {
            database.getAllSkills().value
        }
    }

    private suspend fun insert(skill: Skill) {
        withContext(Dispatchers.IO) {
            database.insertSkill(skill)
        }
    }

    private suspend fun delete(skill: Skill) {
        withContext(Dispatchers.IO) {
            database.deleteSkillById(skill.id)
        }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: SkillsViewModel"
    }
}