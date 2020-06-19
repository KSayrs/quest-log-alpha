package com.example.questlogalpha.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.questlogalpha.quests.DifficultyConverter
import com.example.questlogalpha.quests.QuestsDao
import androidx.sqlite.db.SupportSQLiteDatabase
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.os.AsyncTask.execute
import android.provider.Settings
import android.util.Log
import com.example.questlogalpha.personnage.SkillsDao
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


@Database(entities = [Quest::class, Skill::class, GlobalVariable::class], version = 1, exportSchema = false)
@TypeConverters(DifficultyConverter::class, ZonedDateTimeConverter::class, SkillTypeConverter::class, RewardArrayConverter::class, ObjectiveArrayConverter::class, StoredNotificationArrayConverter::class)
abstract class QuestLogDatabase : RoomDatabase() {

    abstract val questLogDatabaseDao: QuestsDao
    abstract val skillsDatabaseDao: SkillsDao
    abstract val globalVariableDatabaseDao: GlobalVariablesDao

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
                   .addCallback(object : RoomDatabase.Callback(){
                       override fun onCreate(db: SupportSQLiteDatabase) {
                           Log.d("KSLOG", "Database onCreate() override")

                           Executors.newSingleThreadExecutor().execute {
                               Log.d("KSLOG", "Executing thread")
                               getInstance(context).globalVariableDatabaseDao.insertVariable(
                                   GlobalVariable("NotificationId", 100)
                               )
                           }

                           //        // todo pre-populate skills
                   //        // new thread for inserting stuff

                   //     //  Executors.newSingleThreadScheduledExecutor().execute(Runnable {
                   //     //      getInstance(context.applicationContext).skillsDatabaseDao.insertSkill(Skill("Fencing"))
                   //     //      getInstance(context.applicationContext).skillsDatabaseDao.insertSkill(Skill("Dancing"))
                   //     //      getInstance(context.applicationContext).skillsDatabaseDao.insertSkill(Skill("Reputation"))
                   //     //  })


                           super.onCreate(db)

                       }
                   })
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }

}