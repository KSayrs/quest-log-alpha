package com.example.questlogalpha.quests

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.questlogalpha.R
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.skills.SkillsAdapter
import com.example.questlogalpha.skills.SkillsViewModel
import kotlinx.android.synthetic.main.fragment_add_reward.*
import kotlinx.android.synthetic.main.fragment_add_reward.view.*

class AddRewardDialogFragment : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")

        viewModel!!.skills.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter!!.data = it
                Log.d(TAG, "it: " + it)
            }
        })

        return dialogView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog")
        dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_add_reward, null, false)
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
         //  .setSingleChoiceItems(adapter!!, -1,
         //      DialogInterface.OnClickListener {
         //          dialog, whichButton ->
         //               Toast.makeText(context, "Skill $whichButton tapped", Toast.LENGTH_SHORT).show()
         //  })
           .setPositiveButton(android.R.string.ok,
               DialogInterface.OnClickListener { dialog, whichButton ->
                   Toast.makeText(context, "Skill ok tapped", Toast.LENGTH_SHORT).show()
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