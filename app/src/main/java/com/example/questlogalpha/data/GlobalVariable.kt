package com.example.questlogalpha.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "global_variable_table")
data class GlobalVariable(
    @ColumnInfo var name: String,
    @ColumnInfo var value: Int,

    // -------------------- primary key ------------------ //
    @PrimaryKey @ColumnInfo
    val id: String = UUID.randomUUID().toString()
)

/**
 * Data Access Object for the global variable table.
 */
@Dao
interface GlobalVariablesDao {

    /** Select all [GlobalVariable]s from the global variable table. */
    @Query("SELECT * FROM global_variable_table")
    fun getAllVariables(): LiveData<List<GlobalVariable>>

    /** Select a [GlobalVariable] by id. */
    @Query("SELECT * FROM global_variable_table WHERE id = :id")
    fun getVariableById(id: String): GlobalVariable?

    /** Select a [GlobalVariable] by name. */
    @Query("SELECT * FROM global_variable_table WHERE name = :name LIMIT 1")
    fun getVariableWithName(name: String): GlobalVariable?

    /** Insert a [GlobalVariable] into the database. If it already exists, replace it. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVariable(variable: GlobalVariable)

    /**
     * Update a [GlobalVariable].
     * @return the number updated. This should always be 1.
     */
    @Update
    fun updateVariable(variable: GlobalVariable): Int
}
