package com.example.questlogalpha.data

import android.util.Log
import androidx.room.*
import java.lang.Math.exp
import java.util.*
import kotlin.math.pow

/**
 * Immutable model class for a Skill.
 *
 * @param name           name of the skill
 * @param level          current skill level
 * @param currentXP      current skill experience
 * @param nextLevelXP    experience that needs to be reached to level up
 * @param attritionRate  experience lost per week
 * @param iconPath       svg image
 */
@Entity(tableName = "skill_table")
data class Skill (
    @ColumnInfo
    var name: String,

    @ColumnInfo
    var level: Int = 0,

    @ColumnInfo(name="current_xp")
    var currentXP: Int = 0,

    @ColumnInfo(name="next_level_xp")
    var nextLevelXP: Double = 10.0,

    @ColumnInfo(name="attrition_rate")
    var attritionRate: Int = 0, // xp/week

    @ColumnInfo(name="icon_path") var iconPath: String = "",

    @PrimaryKey @ColumnInfo
    val id: String = UUID.randomUUID().toString()
){
    val canLevelUp: Boolean = (currentXP >= nextLevelXP)

    fun onLevelUp(callback:()->Unit){
        level++
        val leveld = level.toDouble()
        //0.04*(a^{3})+0.8*(a^{2})+2*a\ +\ 10
        nextLevelXP = (0.04 * leveld.pow(3)) + (0.8 * leveld.pow(2)+2*leveld) + 10
        Log.d(TAG, "onLevelUp(): nextLevelXP: $nextLevelXP")

        callback()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: Skill"
    }
}