// largely pulled from the architecture to do app

package com.example.questlogalpha.quests

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.questlogalpha.data.Quest
import java.time.ZonedDateTime

/**
 * Data Access Object for the quests table.
 */

@Dao
interface QuestsDao {
    /**
     * Select all quests from the quests table.
     *
     * @return all quests.
     */
    @Query("SELECT * FROM quest_table")
    fun getAllQuests(): LiveData<List<Quest>>

    /**
     * Select a quest by id.
     *
     * @param questId the quest id.
     * @return the quest with questId.
     */
    @Query("SELECT * FROM quest_table WHERE id = :questId")
    fun getQuestById(questId: String): Quest?

    /**
     * Insert a quest into the database. If the quest already exists, replace it.
     *
     * @param quest the quest to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuest(quest: Quest)

    /**
     * Update a quest.
     *
     * @param quest quest to be updated
     * @return the number of quests updated. This should always be 1.
     */
    @Update
    fun updateQuest(quest: Quest): Int

    /**
     * Update the complete status of a quest
     *
     * @param questId    id of the quest
     * @param completed status to be updated
     */
    @Query("UPDATE quest_table SET completed = :completed WHERE id = :questId")
    fun updateCompleted(questId: String, completed: Boolean)

 //   /**
 //    * Set completion date for a quest.
 //    *
 //    * @param questId    id of the quest
 //    * @param dateCompleted date the quest was completed
 //    */
 //   @Query("UPDATE quest_table SET date_completed = :dateCompleted WHERE id = :questId")
 //   suspend fun setCompletionDate(questId: String, dateCompleted: ZonedDateTime)

    /**
     * Delete a quest by id.
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
}