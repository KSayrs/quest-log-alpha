package com.example.questlogalpha.quests

import androidx.annotation.StringDef
import androidx.room.TypeConverter
/**
 * To be used with the filter by. Enums may cause performance issues, but that's an investigation for another time.
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

/*
public class Difficulty {
    // constants
    val TRIVIAL : String = "Trivial"
    val EASY : String = "Simple"
    val MEDIUM : String = "Medium"
    val HARD : String = "Difficult"
    val UNDERTAKING : String = "An Undertaking"

    @StringDef({TRIVIAL, EASY, MEDIUM, HARD, UNDERTAKING})
} */
