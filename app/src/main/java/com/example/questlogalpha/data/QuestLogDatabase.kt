package com.example.questlogalpha.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.questlogalpha.quests.DifficultyConverter
import com.example.questlogalpha.quests.QuestsDao

@Database(entities = [Quest::class], version = 1, exportSchema = false)
@TypeConverters(DifficultyConverter::class, ZonedDateTimeConverter::class)
abstract class QuestLogDatabase : RoomDatabase() {

    abstract val questLogDatabaseDao: QuestsDao

    companion object {

        @Volatile
        private var INSTANCE: QuestLogDatabase? = null

        fun getInstance(context: Context) : QuestLogDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        QuestLogDatabase::class.java,
                        "quest_log_history_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }

}