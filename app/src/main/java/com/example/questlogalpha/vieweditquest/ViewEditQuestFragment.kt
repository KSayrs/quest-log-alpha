package com.example.questlogalpha.vieweditquest

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentViewEditQuestBinding
import com.example.questlogalpha.quests.Difficulty


class ViewEditQuestFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentViewEditQuestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val arguments =  ViewEditQuestFragmentArgs.fromBundle(arguments)
        val viewModelFactory = ViewModelFactory(arguments.questId, dataSource, application)
        val viewEditQuestViewModel = ViewModelProviders.of(this, viewModelFactory).get(ViewEditQuestViewModel::class.java)

        binding.viewEditQuestViewModel = viewEditQuestViewModel
        binding.lifecycleOwner = this

        val spinner: Spinner = binding.difficultySpinner
        spinner.adapter = ArrayAdapter<Difficulty>(application, android.R.layout.simple_spinner_item, Difficulty.values())
        spinner.onItemSelectedListener = viewEditQuestViewModel

        // show the action bar on this page
        val actionbar = (activity as AppCompatActivity).supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.show()

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d("KSLOG", "onCreateOptionsMenu()")
        activity!!.menuInflater.inflate(R.menu.menu_actionbar, menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        Log.d("KSLOG", "onOptionsItemSelected()")
        if (id == R.id.action_done_editing) {
            Toast.makeText(this.context, "toast", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}