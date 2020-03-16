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
import com.example.questlogalpha.skills.SkillsAdapter
import com.example.questlogalpha.skills.SkillsViewModel
import kotlinx.android.synthetic.main.dialog_fragment_add_reward.view.*

class AddRewardDialogFragment : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null

    var onPositiveButtonClicked: ((dictionary: Map<String, Any>) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fragment_add_reward, null, false)
        val binding : DialogFragmentAddRewardBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_fragment_add_reward, null, false)
        binding.lifecycleOwner = this

        val dataSource = QuestLogDatabase.getInstance(activity!!.application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, activity!!.application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        viewModel!!.skills.observe(this, Observer {
            it?.let {
                adapter!!.data = it
                Log.d(TAG, "it: $it")
            }
        })

        adapter = SkillsAdapter()

        val mRecyclerView = dialogView!!.skills_list
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = adapter

        adapter!!.onItemClick = { _, view ->
            view.background = ColorDrawable(resources.getColor(R.color.highlightColor, null))
        }

        adapter!!.onSelectionChange = { view ->
            view.background = ColorDrawable(Color.TRANSPARENT)
        }

        return AlertDialog.Builder(activity)
            .setTitle("Skills")
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok,
              DialogInterface.OnClickListener { dialog, whichButton ->
                  if(adapter?.chosenSkill != null && dialogView!!.skill_amount.text.toString() != "") {

                      binding.chosenSkill = adapter?.chosenSkill
                      binding.amount = dialogView!!.skill_amount.text.toString().toDouble()

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