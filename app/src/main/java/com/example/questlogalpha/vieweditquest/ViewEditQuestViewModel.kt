package com.example.questlogalpha.vieweditquest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.questlogalpha.quests.QuestsDao

class ViewEditQuestViewModel (val database: QuestsDao, application: Application) : AndroidViewModel(application) {

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()
}