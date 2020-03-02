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
import kotlinx.android.synthetic.main.quest_objective_view.view.*
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import androidx.core.view.children
import com.example.questlogalpha.databinding.QuestObjectiveViewBinding
import com.example.questlogalpha.quests.AddRewardDialogFragment
import java.lang.Exception

class ViewEditQuestFragment : Fragment() {

    private var viewModel : ViewEditQuestViewModel ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentViewEditQuestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val arguments =  ViewEditQuestFragmentArgs.fromBundle(arguments)

        val viewModelFactory = ViewModelFactory(arguments.questId, dataSource, null, application)
        val viewEditQuestViewModel = ViewModelProviders.of(this, viewModelFactory).get(ViewEditQuestViewModel::class.java)

        viewModel = viewEditQuestViewModel

        binding.viewEditQuestViewModel = viewEditQuestViewModel
        binding.lifecycleOwner = this

        // set up spinner
        val spinner: Spinner = binding.difficultySpinner
        spinner.adapter = ArrayAdapter<Difficulty>(application, android.R.layout.simple_spinner_item, Difficulty.values())
        spinner.onItemSelectedListener = viewEditQuestViewModel

        // set up rewards adapter
        val rewardsAdapter = RewardsAdapter()
        rewardsAdapter.viewModel = viewEditQuestViewModel
        binding.rewardsList.adapter = rewardsAdapter

        // show the action bar on this page
        val actionbar = (activity as AppCompatActivity).supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.show()

        setHasOptionsMenu(true)

        // this will continue to observe it after stuff has loaded...
        // todo check for performance issues, then maybe come up with a way to remove it after we have loaded the data
        viewEditQuestViewModel.difficulty.observe(viewLifecycleOwner, Observer {
            if (viewEditQuestViewModel.difficulty.value != null) {
                spinner.setSelection(viewEditQuestViewModel.difficulty.value!!.ordinal)
            } else {
                spinner.setSelection(Difficulty.MEDIUM.ordinal)
            }
        })

        // add/update objectives
        viewEditQuestViewModel.modifiedObjective.observe(viewLifecycleOwner, Observer {
            if(viewEditQuestViewModel.objectives.value == null) {
                Log.e(TAG, "viewEditQuestViewModel.objectives is null. Ending observation early.")
                return@Observer
            }
            // todo extend view to allow findInChildren
            val views = binding.objectives.children
            for(objective in viewEditQuestViewModel.objectives.value!!)
            {
                var objectiveBound = false
                for(view in views)
                {
                    val objBinding: QuestObjectiveViewBinding? = DataBindingUtil.getBinding(view)
                    if(objBinding != null && objBinding.objective?.id == objective.id) {
                        objectiveBound = true
                        // todo figure out why this doesn't auto-update
                        objBinding.questObjectiveCheckbox.isChecked = objective.completed
                        break
                    }
                }
                if(objectiveBound) continue

                val objectiveBinding: QuestObjectiveViewBinding = DataBindingUtil.inflate(inflater, R.layout.quest_objective_view, container, false)
                val objView = objectiveBinding.root
                objectiveBinding.objective = objective
                objectiveBinding.viewModel = viewEditQuestViewModel
                binding.objectives.addView(objView)

                objView.delete_objective_icon.setOnClickListener {
                    viewEditQuestViewModel.onObjectiveDeleted(objective)
                    binding.objectives.removeView(objView)
                }

                objView.edit_objective_icon.setOnClickListener {
                    objView.quest_objective_edit_text.isFocusableInTouchMode = true
                    objView.quest_objective_edit_text.requestFocus()
                    val imm = application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.showSoftInput(objView.quest_objective_edit_text, InputMethodManager.SHOW_IMPLICIT)
                }

                objView.quest_objective_edit_text.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        try {
                            viewEditQuestViewModel.onObjectiveEdit(objective, objView.quest_objective_edit_text.text.toString())
                            objView.quest_objective_edit_text.isFocusableInTouchMode = false
                            objView.quest_objective_edit_text.isCursorVisible = false
                            objView.clearFocus()

                            val imm = application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm!!.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

                            binding.viewEditQuestTitleEditText.clearFocus()
                            binding.viewEditQuestDescriptionEditText.clearFocus()
                        } catch (e:Exception) {
                            Log.e(TAG, "onEditorAction: $e")
                        }
                    }
                    false
                }
            }
        })

        // we need to observe both of these because without the full list it won't initialize
        // and without the individual it won't add new ones
        viewEditQuestViewModel.rewards.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observe rewards full list")
            if(viewEditQuestViewModel.rewards.value == null) {
                Log.e(TAG, "viewEditQuestViewModel.rewards is null. Ending observation (rewards) early.")
                return@Observer
            }

            rewardsAdapter.data = viewEditQuestViewModel.rewards.value!!
        })

        viewEditQuestViewModel.modifiedReward.observe(viewLifecycleOwner, Observer {
            if(viewEditQuestViewModel.rewards.value == null) {
                Log.w(TAG, "viewEditQuestViewModel.rewards is null. Ending observation (modifiedReward) early.")
                return@Observer
            }

            rewardsAdapter.data = viewEditQuestViewModel.rewards.value!!
        })

        // add reward button
        binding.addRewardsButton.setOnClickListener{
            val dialog = AddRewardDialogFragment(viewEditQuestViewModel)
            dialog.show(childFragmentManager, "addRewards")
        }

        // navigation
        viewEditQuestViewModel.navigateToQuestsViewModel.observe(viewLifecycleOwner, Observer {
            // todo test with activity!!.onBackPressed()
                if (this.findNavController().currentDestination?.id == R.id.viewEditQuestFragment) {
                    this.findNavController().navigate(
                        ViewEditQuestFragmentDirections.actionViewEditQuestFragmentToMainViewFragment())
                } else {
                    Log.e(TAG,"Current destination is " + this.findNavController().currentDestination?.label + " instead of R.id.viewEditQuestFragment!")
                    return@Observer // this is a hack, otherwise this becomes an infinite loop
                }

                viewEditQuestViewModel.doneNavigating()
        })

        Log.d(TAG,"Current destination is " + this.findNavController().currentDestination?.label)

        // todo extend editText so we don't have to deal with this
        binding.viewEditQuestTitleEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.viewEditQuestTitleEditText.clearFocus()
            }
            false
        }
        binding.viewEditQuestDescriptionEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.viewEditQuestDescriptionEditText.clearFocus()
            }
            false
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
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