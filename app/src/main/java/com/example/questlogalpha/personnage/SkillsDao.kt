package com.example.questlogalpha.personnage

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.questlogalpha.data.Skill

@Dao
interface SkillsDao {
    /**
     * Select all skills from the skill table.
     *
     * @return all skills.
     */
    @Query("SELECT * FROM skill_table")
    fun getAllSkills(): LiveData<List<Skill>>

    /**
     * Select a skill by id.
     *
     * @param skillId the skill id.
     * @return the skill with skillId.
     */
    @Query("SELECT * FROM skill_table WHERE id = :skillId")
    fun getSkillById(skillId: String): Skill?

    /**
     * Insert a skill into the database. If the skill already exists, replace it.
     *
     * @param skill the skill to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSkill(skill: Skill)

    /**
     * Update a skill.
     *
     * @param skill skill to be updated
     * @return the number of skills updated. This should always be 1.
     */
    @Update
    fun updateSkill(skill: Skill): Int

    /**
     * Delete a skill by id.
     *
     * @return the number of skills deleted. This should always be 1.
     */
    @Query("DELETE FROM skill_table WHERE id = :skillId")
    fun deleteSkillById(skillId: String): Int

    /**
     * Reset a skill level and XP to 0.
     *
     * @param skillId id for Skill to be reset
     */
    @Query("UPDATE skill_table SET level = 0, current_xp = 0, next_level_xp = 10 WHERE id = :skillId")
    fun resetSkillLevel(skillId: String)

    /**
     * Delete all skills.
     */
    @Query("DELETE FROM skill_table")
    fun deleteSkills()
}
