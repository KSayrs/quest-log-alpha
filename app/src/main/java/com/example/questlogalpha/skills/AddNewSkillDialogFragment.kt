// todo rename class
package com.example.questlogalpha.skills

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.data.Skill
import kotlinx.android.synthetic.main.dialog_fragment_add_skill.view.*


class AddNewSkillDialogFragment(chosenSkill: Skill?) : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null
    val skill = chosenSkill

    override fun getTheme() = R.style.RoundedCornersDialog2

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fragment_add_skill, null, false)
        val dataSource = QuestLogDatabase.getInstance(activity!!.application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, activity!!.application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        // set up toolbar
        val toolbar = dialogView!!.findViewById<androidx.appcompat.widget.Toolbar>(R.id.dialog_toolbar)
        toolbar.inflateMenu(R.menu.menu_skill_dialog_actionbar)
        if(this.skill == null) toolbar.title = "Add Skill"
        else toolbar.title = "Edit Skill"

        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                // todo add logic to delete skill
                R.id.action_delete_skill -> Toast.makeText(context, "Delete skill tapped", Toast.LENGTH_SHORT).show()
                else -> Log.e(TAG, "onCreateDialog: Some nonexistent action menu item was clicked!")
            }
            true
        }

        return AlertDialog.Builder(activity)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { _, _ ->
                    if(skill == null) {
                        val newSkill = Skill(dialogView!!.new_skill_name.text.toString())
                        viewModel!!.onAddNewSkill(newSkill)
                    }
                    else {
                        // todo add logic to update skill
                        Toast.makeText(context, "TODO: Update skill", Toast.LENGTH_SHORT).show()
                    }
                })
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Toast.makeText(context, "Canceled adding skill", Toast.LENGTH_SHORT).show()
                })
            .create()
    }

   // The EditText appears to have an issue with resetting text in onCreateView. So the solution here is to reset the text in onResume.
   // This is also why we can't use data binding to just set the skill name in the xml
    override fun onResume() {
        super.onResume()
        if(this.skill != null) dialogView?.new_skill_name?.setText(this.skill.name)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: AddNewSkillDialogFragment"
    }
}