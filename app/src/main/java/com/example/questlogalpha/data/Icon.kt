package com.example.questlogalpha.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "icons_table")
data class Icon(
    @ColumnInfo var drawableResource: Int,
    @ColumnInfo var tags: List<String>,

    // -------------------- primary key ------------------ //
    @PrimaryKey @ColumnInfo
    val id: String = UUID.randomUUID().toString()
)

/**
 * Data Access Object for the icons table.
 */
@Dao
interface IconsDao {

    /** Select all [Icon]s from the global variable table. */
    @Query("SELECT * FROM icons_table")
    fun getAllIcons(): LiveData<List<Icon>>

    /** Select an [Icon] by id. */
    @Query("SELECT * FROM icons_table WHERE id = :id")
    fun getIconById(id: String): Icon?

    /** Select an [Icon] by drawable resource. */
    @Query("SELECT * FROM icons_table WHERE drawableResource = :drawableResource LIMIT 1")
    fun getIconWithResourceNumber(drawableResource: Int): Icon?

    /** Search for an [Icon] by tag. */
    @Query("SELECT * FROM icons_table WHERE tags LIKE '%' || :searchTerms || '%'")
    fun searchIcons(searchTerms: String) : LiveData<List<Icon>>

    /** Insert an [Icon] into the database. If it already exists, replace it. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIcon(variable: Icon)

    /**
     * Update an [Icon].
     * @return the number updated. This should always be 1.
     */
    @Update
    fun updateIcon(variable: Icon): Int
}
