package com.example.questlogalpha.data

import android.util.Log
import androidx.room.*
import com.example.questlogalpha.round
import java.util.*
import kotlin.math.pow

/**
 * Immutable model class for a Skill.
 *
 * @param name           name of the skill
 * @param level          current skill level
 * @param currentXP      current skill experience
 * @param attritionRate  experience lost per week
 * @param iconPath       svg image
 */
@Entity(tableName = "skill_table")
data class Skill (
    @ColumnInfo
    var name: String,

    @ColumnInfo
    var level: Int = 0,

    @ColumnInfo(name="skill_type")
    var skillType: SkillType = SkillType.NONE,

    @ColumnInfo(name="current_xp")
    var currentXP: Double = 0.0,

    @ColumnInfo(name="attrition_rate")
    var attritionRate: Int = 0, // xp/week

    @ColumnInfo(name="icon_path") var iconPath: String = "",

    @PrimaryKey @ColumnInfo
    val id: String = UUID.randomUUID().toString()
){
    val nextLevelXP: Double get() = getExperienceForNextLevel(level)

    val canLevelUp: Boolean get(){
        if(getExperienceForNextLevel(level - 2) > currentXP)
        {
            val shouldBe = getExperienceForNextLevel(level-1)
            Log.w(TAG, "CurrentXP ($currentXP) not within level threshold! Adjusting currentXP to match level $level ($shouldBe)")
            currentXP = round(shouldBe, 2)
        }

        return currentXP >= nextLevelXP
    }

    fun onLevelUp(callback:()->Unit = {}){

        if(!canLevelUp) {
            Log.e(TAG, "Current XP not high enough to level! Level: $level, Current: $currentXP, Next: $nextLevelXP")
            return
        }

        while(currentXP >= getExperienceForNextLevel(level)) {
            level++
            Log.d(TAG, "onLevelUp: level: $level, currentXP: $currentXP, nextLevelXP: $nextLevelXP")
        }

        callback()
    }

    // todo replace with geomerty version
    private fun getExperienceForNextLevel(level: Int) : Double {
        return round(((0.04 * level.toDouble()).pow(3)) + ((0.8 * level.toDouble()).pow(2)+2*level.toDouble()) + 1, 2)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: Skill"
    }

}