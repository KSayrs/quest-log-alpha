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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentViewEditQuestBinding
import com.example.questlogalpha.quests.Difficulty
import java.util.zip.Inflater

class ViewEditQuestFragment : Fragment() {

    private var viewModel : ViewEditQuestViewModel ?= null

 //   private val args = ViewEditQuestFragmentArgs.fromBundle(arguments!!)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentViewEditQuestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val arguments =  ViewEditQuestFragmentArgs.fromBundle(arguments)

        val viewModelFactory = ViewModelFactory(arguments.questId, dataSource, application)
        val viewEditQuestViewModel = ViewModelProviders.of(this, viewModelFactory).get(ViewEditQuestViewModel::class.java)

        viewModel = viewEditQuestViewModel

        binding.viewEditQuestViewModel = viewEditQuestViewModel
        binding.lifecycleOwner = this

        // set up spinner
        val spinner: Spinner = binding.difficultySpinner
        spinner.adapter = ArrayAdapter<Difficulty>(application, android.R.layout.simple_spinner_item, Difficulty.values())
        spinner.onItemSelectedListener = viewEditQuestViewModel

        // show the action bar on this page
        val actionbar = (activity as AppCompatActivity).supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.show()

        setHasOptionsMenu(true)

        // todo add objectives for every objective a quest has
        //binding.objectives.addView(View.inflate(application, R.layout.quest_objective_view, container))

        // this will continue to observe it after stuff has loaded...
        // todo check for performance issues, then maybe come up with a way to remove it after we have loaded the data
        viewEditQuestViewModel.difficulty.observe(this, Observer {
            if (viewEditQuestViewModel.difficulty.value != null) {
                spinner.setSelection(viewEditQuestViewModel.difficulty.value!!.ordinal)
            } else {
                spinner.setSelection(Difficulty.MEDIUM.ordinal)
            }
        })

        viewEditQuestViewModel.navigateToQuestsViewModel.observe(this, Observer {
            // todo test with activity!!.onBackPressed()
                if (this.findNavController().currentDestination?.id == R.id.viewEditQuestFragment) {
                    this.findNavController().navigate(
                        ViewEditQuestFragmentDirections.actionViewEditQuestFragmentToMainViewFragment())
                } else {
                    Log.e(TAG,"Current destination is " + this.findNavController().currentDestination?.label + " instead of R.id.viewEditQuestFragment!")
                    return@Observer // this is a hack, otherwisethis becomes an infinite loop
                }

                viewEditQuestViewModel.doneNavigating()
        })

        Log.d(TAG,"Current destination is " + this.findNavController().currentDestination?.label)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d(TAG, "onCreateOptionsMenu()")
        activity!!.menuInflater.inflate(R.menu.menu_actionbar, menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(viewModel == null){
            Toast.makeText(this.context, "Please try again.", Toast.LENGTH_SHORT).show()
            Log.e("ViewEditQuestFragment: onOptionsItemSelected", "viewModel is null!!")
            return false
        }

        val id = item.itemId

        // save quest
        if (id == R.id.action_done_editing) {
            viewModel!!.onSaveQuest()
        }
        return super.onOptionsItemSelected(item)
    }

    // -------------------------- log tag ------------------------------ //

    companion object {
        const val TAG: String = "KSLOG: ViewEditQuestFragment"
    }
}