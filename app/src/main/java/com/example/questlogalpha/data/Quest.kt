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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.questlogalpha.quests.Difficulty
import java.util.*

/**
 * Immutable model class for a Task. In order to compile with Room, we can't use @JvmOverloads to
 * generate multiple constructors.
 *
 * @param title       title of the quest
 * TODO all the rest of the docs
 */
@Entity(tableName = "quest_table")
data class Quest(
    @ColumnInfo
    var title: String,

    @ColumnInfo
    var description: String = "",
    // todo maybe only one reward, and rewards have a type?
    // todo can column be a whole array of things?

  //  @ColumnInfo(name = "skill_rewards")
  //  var skillRewards: ArrayList<Skill> = arrayListOf(),
//
  //  @ColumnInfo(name = "item_rewards")
  //  var itemRewards: ArrayList<Item> = arrayListOf(),

  //  @ColumnInfo
  //  var notifications: ArrayList<Notification> = arrayListOf(), // todo probably will be custom notification type

    @ColumnInfo
    var completed: Boolean = false,

    //- Quests that aren't available until a future date are hidden.
    //- When a quest becomes available, a notification is sent, silently.
    @ColumnInfo
    var visible: Boolean = true,

 //   @ColumnInfo(name = "due_date")
 //   var dueDate: ZonedDateTime? = null,
//
 //   @ColumnInfo(name = "date_available")
 //   var dateAvailable: ZonedDateTime? = null,

    @ColumnInfo
    var difficulty: Difficulty = Difficulty.MEDIUM,

  // @ColumnInfo
  // var objectives: ArrayList<Objective> = arrayListOf(),

    @ColumnInfo
    var questLine: String = "",

    // BI for stats
    @ColumnInfo(name="times_completed")
    var timesCompleted: Int = 0,

    // for an unknown reason the primary key simply WILL NOT WORK with testing if it is an int. String it is.
    @PrimaryKey @ColumnInfo
    val id: String = UUID.randomUUID().toString()

 // @ColumnInfo(name = "date_created")
 // val dateCreated: ZonedDateTime = ZonedDateTime.now(),

 // // Takes the latest date completed
 // @ColumnInfo(name = "date_completed")
 // var dateCompleted: ZonedDateTime? = null
) {
    val isAvailable
        get() = !completed
}
