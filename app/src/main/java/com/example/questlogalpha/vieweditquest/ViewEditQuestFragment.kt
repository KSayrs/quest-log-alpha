package com.example.questlogalpha.vieweditquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentViewEditQuestBinding
import com.example.questlogalpha.quests.QuestsViewModel

class ViewEditQuestFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentViewEditQuestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val viewModelFactory = ViewModelFactory(dataSource, application)
        val viewEditQuestViewModel = ViewModelProviders.of(this, viewModelFactory).get(ViewEditQuestViewModel::class.java)

        binding.viewEditQuestViewModel = viewEditQuestViewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}