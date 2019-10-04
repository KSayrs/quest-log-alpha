package com.example.questlogalpha.data

import android.util.Log
import androidx.room.*
import com.example.questlogalpha.Util
import com.example.questlogalpha.cubicEquation
import com.example.questlogalpha.cubicEquation2
import java.lang.Math.exp
import java.lang.Math.nextDown
import java.util.*
import kotlin.math.floor
import kotlin.math.nextDown
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
    val nextLevelXP: Double get() = ((0.04 * level.toDouble()).pow(3)) + ((0.8 * level.toDouble()).pow(2)+2*level.toDouble()) + 10

    val canLevelUp: Boolean get() = (currentXP >= nextLevelXP)

    fun onLevelUp(callback:()->Unit = {}){

        if(!canLevelUp) {

            Log.e(TAG, "Current XP not high enough to level! Current: $currentXP, Next: $nextLevelXP")
            return
        }
        level++
        
        // f(x) = 0.04(x^3) + 0.8(x^2) + 2x + 10
        val calc = floor(cubicEquation(0.04, 0.08, 2.0, 10.0, currentXP)).toInt()
        val apparentLevel = if(calc >= 0) calc else 0
        if(apparentLevel != level)
        {
            Log.w(TAG, "onLevelUp: level ($level) and XP ($currentXP) mismatch! Adjusting level to $apparentLevel to compensate.")
            level = apparentLevel
        }

        Log.d(TAG, "onLevelUp: level: $level, apparentLevel: $apparentLevel, currentXP: $currentXP, nextLevelXP: $nextLevelXP")
        callback()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: Skill"
    }
}