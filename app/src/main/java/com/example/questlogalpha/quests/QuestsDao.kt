// largely pulled from the architecture to do app

package com.example.questlogalpha.quests

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.data.StoredNotification

/**
 * Data Access Object for the quests table.
 */
@Dao
interface  QuestsDao {
    /**
     * Select all quests from the quests table.
     *
     * @return all quests.
     */
    @Query("SELECT * FROM quest_table")
    fun getAllQuests(): LiveData<List<Quest>>

    /**
     * Select a [Quest] by [questId].
     */
    @Query("SELECT * FROM quest_table WHERE id = :questId")
    fun getQuestById(questId: String): Quest?

    /**
     * Insert a [quest] into the database. If the quest already exists, replace it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuest(quest: Quest)

    /**
     * Update a [quest].
     *
     * @return the number of quests updated. This should always be 1.
     */
    @Update
    fun updateQuest(quest: Quest): Int

    /**
     * Update the [completed] status of a quest with a given [questId]
     */
    @Query("UPDATE quest_table SET completed = :completed WHERE id = :questId")
    fun updateCompleted(questId: String, completed: Boolean)

    /**
     * Update the [iconResourceId] of a quest with a given [questId]
     */
    @Query("UPDATE quest_table SET icon = :iconResourceId WHERE id = :questId")
    fun updateIcon(questId: String, iconResourceId: Int)

 //   /**
 //    * Set completion date for a quest.
 //    *
 //    * @param questId    id of the quest
 //    * @param dateCompleted date the quest was completed
 //    */
 //   @Query("UPDATE quest_table SET date_completed = :dateCompleted WHERE id = :questId")
 //   suspend fun setCompletionDate(questId: String, dateCompleted: ZonedDateTime)

    /**
     * Delete a quest by [questId].
     *
     * @return the number of quests deleted. This should always be 1.
     */
    @Query("DELETE FROM quest_table WHERE id = :questId")
    fun deleteQuestById(questId: String): Int

    /**
     * Delete all quests. //todo clear all data?
     */
    @Query("DELETE FROM quest_table")
    fun deleteQuests()

    // todo should this be removed?
    /**
     * Delete all completed quests from the table.
     *
     * @return the number of quests deleted.
     */
    @Query("DELETE FROM quest_table WHERE completed = 1")
    fun deleteCompletedQuests(): Int

    /**
     * Set the notification data for a quest with a given [questId].
     * @param notifications - array list of [StoredNotification]s
     */
    @Query("UPDATE quest_table SET notifications = :notifications WHERE id = :questId")
    fun setNotifications(questId: String, notifications: ArrayList<StoredNotification>)
}