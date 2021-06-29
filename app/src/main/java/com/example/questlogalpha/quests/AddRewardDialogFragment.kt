package com.example.questlogalpha.quests

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.questlogalpha.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.DialogFragmentAddRewardBinding
import com.example.questlogalpha.skills.AddEditSkillDialogFragment
import com.example.questlogalpha.skills.SkillsAdapter
import com.example.questlogalpha.skills.SkillsViewModel

/** ************************************************************************************************
 * [DialogFragment] to display when adding a reward to a quest.
 * ********************************************************************************************** */
class AddRewardDialogFragment : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null

    var onPositiveButtonClicked: ((dictionary: Map<String, Any>) -> Unit)? = null

    private var _binding: DialogFragmentAddRewardBinding? = null
    private val binding get() = _binding!!

    private var listUpdated = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_fragment_add_reward, null, false)
        binding.lifecycleOwner = this

        val dataSource = QuestLogDatabase.getInstance(requireActivity().application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, null, application = requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        adapter = SkillsAdapter()

        // set up toolbar
        val toolbar = binding.addRewardToolbar
        toolbar.title = getString(R.string.skills)

        // set up recyclerview
        val mRecyclerView = binding.skillsList
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = adapter

        adapter!!.onItemClick = { _, view ->
            view.background = ColorDrawable(resources.getColor(R.color.highlightColor, null))
        }

        adapter!!.onSelectionChange = { view ->
            view.background = ColorDrawable(Color.TRANSPARENT)
        }

        adapter!!.onViewAttached = { view ->
            if(listUpdated) {
                view.background = ColorDrawable(resources.getColor(R.color.highlightColor, null))
                adapter!!.overrideLastClicked(view)
            }
        }

        // observe skills
        viewModel!!.skills.observe(this, Observer {
            it?.let {
                adapter!!.data = it
                Log.d(TAG, "it: $it")
            }
        })

        // set up "add new" button
       binding.addNewRewardButton.setOnClickListener {
            val addSkillDialog = AddEditSkillDialogFragment()

            addSkillDialog.onPositiveButtonClicked = { skill ->
                adapter!!.chosenSkill = skill
                listUpdated = true
            }

            addSkillDialog.show(childFragmentManager, "addSkillFromRewardScreen")
        }

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok,
              DialogInterface.OnClickListener { dialog, whichButton ->
                  if(adapter?.chosenSkill != null && binding.skillAmount.text.toString() != "") {

                      binding.chosenSkill = adapter?.chosenSkill
                      binding.amount = binding.skillAmount.text.toString().toDouble()

                      onPositiveButtonClicked?.invoke(mapOf("skill" to binding.chosenSkill!!, "amount" to binding.amount!!))
                      Toast.makeText(context, "Skill ${binding.chosenSkill?.name} tapped for ${binding.amount}", Toast.LENGTH_SHORT).show()
                  }
              })
            .setNegativeButton(R.string.cancel,
              DialogInterface.OnClickListener { dialog, id ->
                  Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show()
              })
            .create()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: AddRewardDialogFragment"
    }
}