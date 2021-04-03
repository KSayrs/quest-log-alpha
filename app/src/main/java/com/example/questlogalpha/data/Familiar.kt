package com.example.questlogalpha.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "familiars_table")
data class Familiar(
    @ColumnInfo(name="drawable_resource") var drawableResource:Int = 0,

 //  @ColumnInfo(name="unlock_condtion") var unlockCondition: UnlockCondition = UnlockCondition.LEVEL_REACHED,
 //  @ColumnInfo(name="unlock_value") var unlockValue: Int = 0,
 //  @ColumnInfo var unlocked: Boolean = false,
 //  @ColumnInfo var current: Boolean = false,

    // -------------------- primary key ------------------ //
    @PrimaryKey @ColumnInfo var id: String = UUID.randomUUID().toString()
)

/**
 * Data Access Object for the familiars table.
 */
@Dao
interface FamiliarsDao {
    /**
     * Select all familiars from the familiar table.
     *
     * @return all familiars.
     */
    @Query("SELECT * FROM familiars_table")
    fun getAllFamiliars(): LiveData<List<Familiar>>

    /**
     * Select a familiar by id.
     *
     * @param familiarId the familiar id.
     * @return the familiar with familiarId.
     */
    @Query("SELECT * FROM familiars_table WHERE id = :familiarId")
    fun getFamiliarById(familiarId: String): Familiar?

    /**
     * Select familiar(s) by [drawableId].
     *
     * @return list of familiars with familiarId.
     */
    @Query("SELECT * FROM familiars_table WHERE drawable_resource = :drawableId")
    fun getFamiliarByResourceId(drawableId: Int): List<Familiar>?

    /**
     * Select all familiar images from the familiar table.
     *
     * @return all familiar drawable resource Ids.
     */
    @Query("SELECT drawable_resource FROM familiars_table")
    fun getAllFamiliarImages(): LiveData<List<Int>>

    /**
     * Insert a familiar into the database. If the familiar already exists, replace it.
     *
     * @param familiar the familiar to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFamiliar(familiar: Familiar)

    /**
     * Update a familiar.
     *
     * @param familiar familiar to be updated
     * @return the number of familiars updated. This should always be 1.
     */
    @Update
    fun updateFamiliar(familiar: Familiar): Int

    /**
     * Delete a familiar by id.
     *
     * @return the number of familiars deleted. This should always be 1.
     */
    @Query("DELETE FROM familiars_table WHERE id = :familiarId")
    fun deleteFamiliarById(familiarId: String): Int

    /** Delete all familiars. */
    @Query("DELETE FROM familiars_table")
    fun deleteAllFamiliars()
}