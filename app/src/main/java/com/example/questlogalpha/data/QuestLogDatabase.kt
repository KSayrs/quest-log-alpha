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

                           // notification id
                           Executors.newSingleThreadExecutor().execute {
                               Log.d("KSLOG", "Executing thread 1")
                               getInstance(context).globalVariableDatabaseDao.insertVariable(
                                   GlobalVariable("NotificationId", 100)
                               )
                           }

                           // icons
                           Executors.newSingleThreadExecutor().execute {
                               Log.d("KSLOG", "Executing thread 2")

                               val list = populateIconList()
                               for (icon in list)
                               {
                                   getInstance(context).iconsDatabaseDao.insertIcon(icon)
                               }
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

        private fun populateIconList() : ArrayList<Icon> {
            val list = arrayListOf<Icon>()

            list.add(Icon(R.drawable.ic_clock, listOf("clock", "timer")) )
            list.add(Icon(R.drawable.ic_cog, listOf("cog", "gear", "steampunk")) )
            list.add(Icon(R.drawable.ic_scroll_quill, listOf("scroll", "quill", "writing", "paper", "pen")) )
            list.add(Icon(R.drawable.ic_clothes, listOf("clothes", "shirt", "pants")) )
            list.add(Icon(R.drawable.ic_daggers, listOf("daggers", "sword", "knives")) )
            list.add(Icon(R.drawable.ic_ample_dress, listOf("dress", "clothes")) )
            list.add(Icon(R.drawable.ic_belt_buckles, listOf("belt", "buckles")) )
            list.add(Icon(R.drawable.ic_coins, listOf("coins", "money", "cash", "gold", "pile of coins")) )
            list.add(Icon(R.drawable.ic_gamepad, listOf("gamepad", "games", "video games")) )
            list.add(Icon(R.drawable.ic_gloves, listOf("gloves", "clothes")) )
            list.add(Icon(R.drawable.ic_hanger, listOf("hanger", "laundry", "clothes")) )
            list.add(Icon(R.drawable.ic_hearts, listOf("hearts", "love")) )
            list.add(Icon(R.drawable.ic_house, listOf("house", "home")) )
            list.add(Icon(R.drawable.ic_id_card, listOf("card", "id")) )
            list.add(Icon(R.drawable.ic_magnifying_glass, listOf("magnifying glass", "glass", "investigate", "spy", "look", "search")) )
            list.add(Icon(R.drawable.ic_open_book, listOf("book", "open book", "page")) )
            list.add(Icon(R.drawable.ic_envelope, listOf("envelope", "mail")) )
            list.add(Icon(R.drawable.ic_pencil, listOf("pencil", "art")) )
            list.add(Icon(R.drawable.ic_lightning_saber, listOf("sword", "saber", "lightning", "blade")) )
            list.add(Icon(R.drawable.ic_paint_brush, listOf("paintbrush", "art")) )
            list.add(Icon(R.drawable.ic_pawn, listOf("pawn", "chess", "games")) )
            list.add(Icon(R.drawable.ic_perspective_dice_six_faces_three, listOf("dice", "games", "die")) )
            list.add(Icon(R.drawable.ic_pie_chart, listOf("chart", "pie", "graph")) )
            list.add(Icon(R.drawable.ic_pin, listOf("pin", "tack")) )
            list.add(Icon(R.drawable.ic_scythe, listOf("scythe", "sickle")) )
            list.add(Icon(R.drawable.ic_sewing_needle, listOf("sewing", "needle", "thread")) )
            list.add(Icon(R.drawable.ic_scales, listOf("scales", "balance", "libra")) )
            list.add(Icon(R.drawable.ic_sands_of_time, listOf("hourglass", "timer")) )
            list.add(Icon(R.drawable.ic_wool, listOf("wool", "ball of yarn", "knitting")) )
            list.add(Icon(R.drawable.ic_winter_gloves, listOf("gloves", "mittens", "winter", "clothes")) )
            list.add(Icon(R.drawable.ic_winter_hat, listOf("hat", "winter", "clothes")) )
            list.add(Icon(R.drawable.ic_wallet, listOf("wallet", "money")) )
            list.add(Icon(R.drawable.ic_stopwatch, listOf("stopwatch", "clock", "time")) )
            list.add(Icon(R.drawable.ic_stabbed_note, listOf("note", "parchment", "paper", "stabbed", "scroll")) )

            return list
        }
    }
}