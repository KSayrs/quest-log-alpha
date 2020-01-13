package com.example.questlogalpha.quests

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.questlogalpha.R
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.questlogalpha.ITalkToDialogs
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentAddRewardBinding
import com.example.questlogalpha.databinding.FragmentViewEditQuestBinding
import com.example.questlogalpha.skills.SkillsAdapter
import com.example.questlogalpha.skills.SkillsViewModel
import com.example.questlogalpha.vieweditquest.ViewEditQuestViewModel
import kotlinx.android.synthetic.main.fragment_add_reward.*
import kotlinx.android.synthetic.main.fragment_add_reward.view.*
import java.util.*

class AddRewardDialogFragment(vm: ITalkToDialogs) : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var viewEditViewModel : ITalkToDialogs? = vm
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel!!.skills.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter!!.data = it
                Log.d(TAG, "it: " + it)
            }
        })

        return dialogView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_add_reward, null, false)
        val binding : FragmentAddRewardBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_add_reward, null, false)
        binding.lifecycleOwner = this

        val dataSource = QuestLogDatabase.getInstance(activity!!.application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, activity!!.application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SkillsViewModel::class.java)

        adapter = SkillsAdapter()

        val mRecyclerView = dialogView!!.skills_list
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = adapter

        return AlertDialog.Builder(activity)
            .setTitle("Skills")
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok,
              DialogInterface.OnClickListener { dialog, whichButton ->

                  if(adapter?.chosenSkill != null && dialogView!!.skill_amount.text.toString() != "") {

                      binding.chosenSkill = adapter?.chosenSkill
                      binding.amount = dialogView!!.skill_amount.text.toString().toDouble()

                      viewEditViewModel?.onPositiveButtonClicked(mapOf("skill" to binding.chosenSkill!!, "amount" to binding.amount!!))
                      Toast.makeText(context, "Skill ${binding.chosenSkill?.name} tapped for ${binding.amount}", Toast.LENGTH_SHORT).show()
                  }
              })
            .setNegativeButton(R.string.abandon,
              DialogInterface.OnClickListener { dialog, id ->
                  Toast.makeText(context, "Skill cancel tapped", Toast.LENGTH_SHORT).show()
              })
            .create()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: AddRewardDialogFragment"
    }
}