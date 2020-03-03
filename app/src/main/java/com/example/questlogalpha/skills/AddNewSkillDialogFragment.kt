package com.example.questlogalpha.skills

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.data.Skill
import kotlinx.android.synthetic.main.dialog_fragment_add_skill.view.*


class AddNewSkillDialogFragment : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fragment_add_skill, null, false)
        val dataSource = QuestLogDatabase.getInstance(activity!!.application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, activity!!.application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        val toolbar = dialogView!!.findViewById<androidx.appcompat.widget.Toolbar>(R.id.dialog_toolbar)
        toolbar.inflateMenu(R.menu.menu_skill_dialog_actionbar)
        toolbar.title = "Add Skill"

        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_delete_skill -> Toast.makeText(context, "Delete skill tapped", Toast.LENGTH_SHORT).show()
                else -> Log.e(TAG, "onCreateDialog: Some nonexistent action menu item was clicked!")
            }
            true
        }

        return AlertDialog.Builder(activity)
            //.setTitle("Add Skill")
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { _, _ ->
                    val newSkill = Skill(dialogView!!.new_skill_name.text.toString())
                    viewModel!!.onAddNewSkill(newSkill)
                })
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Toast.makeText(context, "Canceled adding skill", Toast.LENGTH_SHORT).show()
                })
            .create()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: AddNewSkillDialogFragment"
    }
}