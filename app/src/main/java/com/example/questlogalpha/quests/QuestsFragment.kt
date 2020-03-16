package com.example.questlogalpha.quests

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentQuestsBinding
import com.example.questlogalpha.skills.SkillsViewModel
import com.example.questlogalpha.ui.main.MainViewFragmentDirections

/** ************************************************************************************************
 * [Fragment] to display all quests in the database.
 * ********************************************************************************************** */
class QuestsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e("QuestsFragment.kt: onCreate: ", "supportActionBar is null.")
        else  supportActionBar.hide()
    }

    // Hide the navigation bar when we're on this activity
    override fun onResume() {
        super.onResume()
        Log.d("$TAG: onResume", " called")

        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e("$TAG: onResume: ", "supportActionBar is null.")
        else if(supportActionBar.isShowing) supportActionBar.hide()
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentQuestsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quests, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val skillsDataSource = QuestLogDatabase.getInstance(application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", dataSource, skillsDataSource, application)

        val questsViewModel = ViewModelProvider(this, viewModelFactory).get(QuestsViewModel::class.java)
        val skillsViewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        val adapter = QuestsAdapter()
        binding.questList.adapter = adapter

        binding.questsViewModel = questsViewModel
        binding.lifecycleOwner = this

        // Add an Observer on the state variable for Navigating when add quest button is pressed.
        questsViewModel.navigateToViewEditQuest.observe(viewLifecycleOwner, Observer { questId ->
            questId?.let {
                // We need to get the navController from this, because button is not ready, and it
                // just has to be a view. For some reason, this only matters if we hit stop again
                // after using the back button, not if we hit stop and choose a quality.
                // Also, in the Navigation Editor, for Quality -> Tracker, check "Inclusive" for
                // popping the stack to get the correct behavior if we press stop multiple times
                // followed by back.
                // Also: https://stackoverflow.com/questions/28929637/difference-and-uses-of-oncreate-oncreateview-and-onactivitycreated-in-fra

                if (this.findNavController().currentDestination?.id == R.id.mainViewFragment) {
                    this.findNavController().navigate(
                        MainViewFragmentDirections.actionMainViewFragmentToViewEditQuestFragment(questId))
                }
                else {
                    Log.e(TAG,"Current destination is " + this.findNavController().currentDestination?.label + " instead of R.id.mainViewFragment!")
                }
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                questsViewModel.doneNavigating()
            }
        })

        // assign the quests to the adapter
        questsViewModel.quests.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
                adapter.onQuestTitleTapped = { quest ->
                    questsViewModel.onSetQuestCompletion(quest.id, !quest.completed)

                    if(!quest.completed) { // crossing out a quest
                        for (reward in quest.rewards) {
                            skillsViewModel.addExperience(reward.id, reward.amount)
                        }
                    }
                    else { // undo-ing an accident
                       for (reward in quest.rewards) {
                           skillsViewModel.removeExperience(reward.id, reward.amount)
                       }
                    }
                }
                adapter.viewModel = binding.questsViewModel
            }
        })

        return binding.root
    }
    // -------------------------- log tag ------------------------------ //

    companion object {
        const val TAG: String = "KSLOG: QuestsFragment"
    }
}
