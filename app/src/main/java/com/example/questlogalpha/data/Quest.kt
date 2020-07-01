package com.example.questlogalpha.data

/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.questlogalpha.quests.Difficulty
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

/**
 * Immutable model class for a Quest. In order to compile with Room, we can't use @JvmOverloads to
 * generate multiple constructors.
 *
 * @param title           title of the quest
 * @param description     description for the quest
 * @param icon            icon resource id for the quest
 * @param completed       is the quest complete
 * @param visible         is the quest visible by default in the recyclerView
 * @param difficulty      difficulty of the quest
 * @param questLine       id of the questLine this quest belongs to
 * @param timesCompleted  how many times has the quest been completed
 * @param dateCreated     date the quest was created
 */
@Entity(tableName = "quest_table")
data class Quest(
    @ColumnInfo var title: String,
    @ColumnInfo var description: String = "",

    @ColumnInfo var icon: Int = 0,

    @ColumnInfo(name = "rewards") var rewards: ArrayList<SkillReward> = arrayListOf(),

    @ColumnInfo var notifications: ArrayList<StoredNotification> = arrayListOf(),

    @ColumnInfo var completed: Boolean = false,

    //- Quests that aren't available until a future date are hidden.
    //- When a quest becomes available, a notification is sent, silently.
    @ColumnInfo var visible: Boolean = true,

    // todo come put with a better name for due date
    @ColumnInfo(name = "due_date") var dueDate: ZonedDateTime? = null,


    // todo date available
 //   @ColumnInfo(name = "date_available")
 //   var dateAvailable: ZonedDateTime? = null,

    @ColumnInfo var difficulty: Difficulty = Difficulty.MEDIUM,

    @ColumnInfo var objectives: ArrayList<Objective> = arrayListOf(),

    @ColumnInfo var questLine: String = "",

    // ------------------- BI for stats ---------------------- //
    @ColumnInfo(name="times_completed") var timesCompleted: Int = 0,

    @ColumnInfo(name = "date_created") val dateCreated: ZonedDateTime = ZonedDateTime.now(),

    // Takes the latest date completed
    // todo date completed
    // @ColumnInfo(name = "date_completed") var dateCompleted: ZonedDateTime? = null

    // -------------------- primary key ------------------ //
    // for an unknown reason the primary key simply WILL NOT WORK with testing if it is an int. String it is.
    @PrimaryKey @ColumnInfo
    val id: String = UUID.randomUUID().toString()
) {
    val isAvailable
        get() = !completed
}
