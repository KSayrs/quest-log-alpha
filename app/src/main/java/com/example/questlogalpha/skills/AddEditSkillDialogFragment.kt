package com.example.questlogalpha.skills

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.databinding.DialogFragmentAddEditSkillBinding
import com.example.questlogalpha.focus
import com.example.questlogalpha.quests.QuestsViewModel

/** ************************************************************************************************
 * The dialog fragment that pops up when adding or editing a [chosenSkill].
 * ********************************************************************************************** */
class AddEditSkillDialogFragment(chosenSkill: Skill? = null) : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null
    val skill = chosenSkill

    var onPositiveButtonClicked: ((skill: Skill) -> Unit)? = null

    private var questsViewModel: QuestsViewModel? = null
    private var quests: List<Quest>? = null

    private var _binding: DialogFragmentAddEditSkillBinding? = null
    private val binding get() = _binding!!

    // public override functions
    // ---------------------------------------------------------------- //

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = LayoutInflater.from(context)
        _binding = DataBindingUtil.inflate(layoutInflater,R.layout.dialog_fragment_add_edit_skill, null, false)

        val dataSource = QuestLogDatabase.getInstance(requireActivity().application).skillsDatabaseDao
        val questsDataSource = QuestLogDatabase.getInstance(requireActivity().application).questLogDatabaseDao
        val iconsDataSource = QuestLogDatabase.getInstance(requireActivity().application).iconsDatabaseDao
        val familiarsDataSource = QuestLogDatabase.getInstance(requireActivity().application).familiarsDatabaseDao
        val globalVariables = QuestLogDatabase.getInstance(requireActivity().application).globalVariableDatabaseDao
        val viewModelFactory = ViewModelFactory("", questsDataSource, dataSource, familiarsDataSource, globalVariables, iconsDataSource = iconsDataSource, application = requireActivity().application)

        questsViewModel = ViewModelProvider(this, viewModelFactory).get(QuestsViewModel::class.java)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        // set up toolbar
        val toolbar = binding.dialogToolbar
        toolbar.title = if(this.skill == null) getString(R.string.add_skill) else getString(R.string.edit_skill)

        if(this.skill != null) {
            toolbar.inflateMenu(R.menu.menu_skill_dialog_actionbar)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete_skill -> {
                        removeSkillFromQuestRewards()
                        viewModel!!.onDeleteSkill(this.skill)
                        dismiss()
                    }
                    else -> Log.e(TAG,"onCreateDialog: Some nonexistent action menu item was clicked!")
                }
                true
            }
        }

        // grab quest data
        questsViewModel?.quests?.observe(this, Observer {
            it?.let {
                quests = it
            }
        })

        // make Dialog
        val acceptMessage:String = if(this.skill == null) getString(R.string.add) else getString(R.string.save)
        val builder = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setPositiveButton(acceptMessage,
                DialogInterface.OnClickListener { _, _ ->
                    if(this.skill == null) {
                        val newSkill = Skill(binding.newSkillName.text.toString())
                        viewModel!!.onAddNewSkill(newSkill)
                        onPositiveButtonClicked?.invoke(newSkill)
                    }
                    else {
                        this.skill.name = binding.newSkillName.text.toString()
                        viewModel!!.onEditSkill(this.skill)
                        onPositiveButtonClicked?.invoke(this.skill)
                    }
                })
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show()
                })
            .create()

        // if it's a new skill, focus the text
        if(this.skill == null) {
            builder.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            binding.newSkillName.focus(requireActivity().application)
        }
        return builder
    }

   // The EditText appears to have an issue with resetting text in onCreateView. So the solution here is to reset the text in onResume.
   // This is also why we can't use data binding to just set the skill name in the xml
    override fun onResume() {
        super.onResume()
        if(this.skill != null) binding.newSkillName.setText(this.skill.name)
    }

    // private functions
    // ---------------------------------------------------------------- //

    /** Iterates through all quests in the database and removes rewards that would reward this [skill] */
    private fun removeSkillFromQuestRewards()
    {
        if(questsViewModel == null) {
            Log.w(TAG, "Quests View Model is null! Quests that reward this skill will not have the reward removed.")
            return
        }
        if(quests == null) {
            Log.d(TAG, "quests is null! Quests that reward this skill will not have the reward removed.")
            return
        }

        for(quest in quests!!) {
            val rewards = quest.rewards
            for(reward in rewards) {
                // reward ids always match the id of the skill or item it is rewarding
                if(reward.id == this.skill!!.id) {
                    quest.rewards.remove(reward)
                    questsViewModel!!.onUpdateQuest(quest)
                }
            }
        }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: AddEditSkillDialogFragment"
    }
}