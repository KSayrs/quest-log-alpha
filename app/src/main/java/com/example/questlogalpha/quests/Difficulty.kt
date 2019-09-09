package com.example.questlogalpha.quests

import androidx.room.TypeConverter
/**
 * To be used with the filter by.
 */
enum class Difficulty {
    TRIVIAL,
    EASY,
    MEDIUM,
    HARD,
    UNDERTAKING
}

class DifficultyConverter {

    @TypeConverter
    fun storedStringToDifficulty(value: String): Difficulty {
        return Difficulty.valueOf(value)
    }

    @TypeConverter
    fun difficultyToStoredString(difficulty: Difficulty): String {
        return difficulty.name
    }
}
