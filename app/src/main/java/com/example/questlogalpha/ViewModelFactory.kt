// Taken from android blueprint to do app
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
package com.example.questlogalpha

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.personnage.SkillsDao
import com.example.questlogalpha.quests.QuestsDao
import com.example.questlogalpha.quests.QuestsViewModel
import com.example.questlogalpha.skills.SkillsViewModel
import com.example.questlogalpha.vieweditquest.ViewEditQuestViewModel
import java.lang.Appendable

/** ************************************************************************************************
 * Factory ([ViewModelProvider.NewInstanceFactory]) for all ViewModels.
 * ********************************************************************************************** */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val questId: String,
    private val questsDataSource: QuestsDao? = null,
    private val skillsDataSource: SkillsDao? = null,
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(QuestsViewModel::class.java) ->
                    QuestsViewModel(questsDataSource!!)
                isAssignableFrom(ViewEditQuestViewModel::class.java) ->
                    ViewEditQuestViewModel(questId, questsDataSource!!, application)
                isAssignableFrom(SkillsViewModel::class.java) ->
                    SkillsViewModel(skillsDataSource!!)
            //   isAssignableFrom(TasksViewModel::class.java) ->
            //       TasksViewModel(tasksRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
