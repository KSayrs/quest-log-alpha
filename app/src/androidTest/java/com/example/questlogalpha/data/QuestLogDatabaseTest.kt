package com.example.questlogalpha.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.questlogalpha.quests.QuestsDao
import com.example.questlogalpha.data.LiveDataTestUtil.getValue
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class QuestLogDatabaseTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var questsDao: QuestsDao
    private lateinit var db: QuestLogDatabase

    private lateinit var testQuest: Quest

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, QuestLogDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        questsDao = db.questLogDatabaseDao

        testQuest = Quest("test", "TestQuest")
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    //-------------------------

    @Test
    @Throws(Exception::class)
    fun addAndGetQuest(){
        val quest = Quest("test","Test")
        questsDao.insertQuest(quest)
        val justMade = questsDao.getQuestById("test")
        assertEquals(quest, justMade)
    }

    @Test
    @Throws(Exception::class)
    fun addAndUpdateQuest(){
        val quest = Quest("test","Test")
        questsDao.insertQuest(quest)
        quest.completed = true
        questsDao.updateQuest(quest)

        val retrievedQuest = questsDao.getQuestById(testQuest.id)
        assertEquals(retrievedQuest, quest)
        assertEquals(true, retrievedQuest?.completed)
    }

    //-------------------------

    @Test
    @Throws(Exception::class)
    fun addTestQuest(){
        questsDao.insertQuest(testQuest)
        val retrieved = questsDao.getQuestById(testQuest.id)
        assertEquals(testQuest, retrieved)
    }

    @Test
    @Throws(Exception::class)
    fun updateQuest(){
        questsDao.insertQuest(testQuest)
        testQuest.completed = true
        questsDao.updateQuest(testQuest)
        val retrieved = questsDao.getQuestById(testQuest.id)
        assertEquals(retrieved, testQuest)
        assertEquals(true, retrieved?.completed)
    }

    @Test
    @Throws(Exception::class)
    fun updateCompleted(){
        testQuest.completed = false
        questsDao.insertQuest(testQuest)
        questsDao.updateCompleted(testQuest.id, true)
        val retrieved = questsDao.getQuestById(testQuest.id)
        assertEquals(true, retrieved?.completed)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDeleteTestQuest (){
        questsDao.insertQuest(testQuest)
        questsDao.deleteQuestById(testQuest.id)
        val retrieved = getValue(questsDao.getQuests())
        assertEquals(0, retrieved.size)
    }
}