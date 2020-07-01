package com.example.questlogalpha.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.questlogalpha.quests.DifficultyConverter
import com.example.questlogalpha.quests.QuestsDao
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log
import com.example.questlogalpha.R
import com.example.questlogalpha.personnage.SkillsDao
import java.util.concurrent.Executors


@Database(entities = [Quest::class, Skill::class, GlobalVariable::class, Icon::class], version = 1, exportSchema = false)
@TypeConverters(
    DifficultyConverter::class,
    ZonedDateTimeConverter::class,
    SkillTypeConverter::class,
    RewardArrayConverter::class,
    ObjectiveArrayConverter::class,
    StoredNotificationArrayConverter::class,
    ListConverter::class
)
abstract class QuestLogDatabase : RoomDatabase() {

    abstract val questLogDatabaseDao: QuestsDao
    abstract val skillsDatabaseDao: SkillsDao
    abstract val globalVariableDatabaseDao: GlobalVariablesDao
    abstract val iconsDatabaseDao: IconsDao

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
                               Log.d("KSLOG", "Executing thread 1")
                               getInstance(context).globalVariableDatabaseDao.insertVariable(
                                   GlobalVariable("NotificationId", 100)
                               )
                           }

                           // icons
                           Executors.newSingleThreadExecutor().execute {
                               Log.d("KSLOG", "Executing thread 2")
                               getInstance(context).iconsDatabaseDao.insertIcon( Icon(R.drawable.ic_clock, listOf("clock", "time", "timer")) )
                               getInstance(context).iconsDatabaseDao.insertIcon( Icon(R.drawable.ic_cog, listOf("cog", "gear", "clockwork", "steampunk")) )
                               getInstance(context).iconsDatabaseDao.insertIcon( Icon(R.drawable.ic_scroll_quill, listOf("scroll", "quill", "writing")) )
                           }

                           // todo insert icon data
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