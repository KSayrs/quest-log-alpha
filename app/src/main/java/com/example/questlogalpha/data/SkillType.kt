// TODO we want to be able to add and remove skill types. Delete this and replace with something mutable, a global array or within a larger personage class, perhaps

package com.example.questlogalpha.data

import androidx.room.TypeConverter

/* To be used as a further way to examine data and skills. Enums may cause performance issues, but that's an investigation for another time. */
enum class SkillType {
    NONE,
    PHYSICAL,
    EMOTION,
    KNOWLEDGE,
    SOCIAL,
    CRAFTING,
    LIFE
}

class SkillTypeConverter {

    @TypeConverter
    fun storedStringToSkill(value: String): SkillType {
        return SkillType.valueOf(value)
    }

    @TypeConverter
    fun skillTypeToStoredString(skillType: SkillType): String {
        return skillType.name
    }
}
