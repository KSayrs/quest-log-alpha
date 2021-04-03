package com.example.questlogalpha.quests

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentQuestsBinding
import com.example.questlogalpha.skills.SkillsViewModel
import com.example.questlogalpha.toast
import com.example.questlogalpha.ui.main.MainViewFragmentDirections


/** ************************************************************************************************
 * [Fragment] to display all quests in the database.
 * ********************************************************************************************** */
class QuestsFragment(private val toolbar: androidx.appcompat.widget.Toolbar) : Fragment() {

    private var viewModel: QuestsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e("QuestsFragment.kt: onCreate: ", "supportActionBar is null.")
        else  supportActionBar.hide()

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e("QuestsFragment.kt: onCreate: ", "supportActionBar is null.")
        else  supportActionBar.hide()

        setHasOptionsMenu(true)
    }

    // this has to be here for onPrepare to be called
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) { super.onCreateOptionsMenu(menu, inflater) }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (viewModel == null) Log.e(TAG, "onPrepareOptionsMenu: viewmodel is not ready yet")
        else {
            if (viewModel!!.viewingCompleted.value!!) {
                if(!toolbar.menu.findItem(R.id.action_show_incomplete).isVisible) toolbar.menu.findItem(R.id.action_show_incomplete).isVisible = true
                toolbar.menu.findItem(R.id.action_show_completed).isVisible = false
            }
            else {
                toolbar.menu.findItem(R.id.action_show_incomplete).isVisible = false
                if(!toolbar.menu.findItem(R.id.action_show_completed).isVisible) toolbar.menu.findItem(R.id.action_show_completed).isVisible = true
            }
        }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding: FragmentQuestsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quests, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val skillsDataSource = QuestLogDatabase.getInstance(application).skillsDatabaseDao
        val iconsDataSource = QuestLogDatabase.getInstance(application).iconsDatabaseDao
        val globalVariables = QuestLogDatabase.getInstance(application).globalVariableDatabaseDao
        val familiarsDataSource = QuestLogDatabase.getInstance(application).familiarsDatabaseDao

        val viewModelFactory = ViewModelFactory("", dataSource, skillsDataSource, familiarsDataSource, globalVariables, iconsDataSource, application = application)

        val questsViewModel = ViewModelProvider(this, viewModelFactory).get(QuestsViewModel::class.java)
        val skillsViewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        viewModel = questsViewModel

        val adapter = QuestsAdapter(childFragmentManager)
        adapter.viewModel = questsViewModel

        adapter.onQuestTitleTapped = { quest ->
            questsViewModel.onSetQuestCompletion(quest.id, !quest.completed)

            if (!quest.completed) { // crossing out a quest
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

        binding.questList.adapter = adapter

        binding.questsViewModel = questsViewModel
        binding.lifecycleOwner = this

        // set up toolbar
        toolbar.inflateMenu(R.menu.menu_questlist_actionbar)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_show_incomplete -> {
                    viewModel!!.viewingCompleted.value = false
                    adapter.data = viewModel!!.quests.value!!
                }
                R.id.action_show_completed -> {
                    viewModel!!.viewingCompleted.value = true
                    adapter.data = viewModel!!.quests.value!!
                }
                else -> {
                    "Something went wrong".toast(context!!)
                    Log.e(TAG, "onCreateDialog: Some nonexistent action menu item was clicked!")
                }
            }

            activity!!.invalidateOptionsMenu()

            true
        }
        setHasOptionsMenu(true)

        val familiarSpinner: Spinner = binding.currentFamiliar
        familiarSpinner.onItemSelectedListener = viewModel

        // set the spinner data on load
        questsViewModel.familiarLoaded.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "loaded: it: $it")
            if(it) {
                val familiarAdapter = FamiliarSpinnerAdapter(
                    context!!,
                    (questsViewModel.familiarImages.value!!).toTypedArray()
                )

                familiarSpinner.adapter = familiarAdapter
                familiarSpinner.setSelection(questsViewModel.getCurrentFamiliarOrdinal(familiarAdapter))
            }
        })


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
                        MainViewFragmentDirections.actionMainViewFragmentToViewEditQuestFragment(questId)
                    )
                }
                else {
                    Log.e(TAG, "Current destination is " + this.findNavController().currentDestination?.label + " instead of R.id.mainViewFragment!")
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
                questsViewModel.questsFromFragment = it
            }
        })

        return binding.root
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsFragment"
    }

}
