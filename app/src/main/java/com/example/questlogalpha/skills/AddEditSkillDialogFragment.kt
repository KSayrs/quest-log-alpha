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
import com.example.questlogalpha.setClearFocusOnDone
import kotlinx.android.synthetic.main.dialog_fragment_add_edit_skill.view.*

class AddEditSkillDialogFragment(chosenSkill: Skill?) : DialogFragment() {
    var viewModel : SkillsViewModel? = null
    var adapter : SkillsAdapter? = null
    var dialogView : View? = null
    val skill = chosenSkill

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fragment_add_edit_skill, null, false)
        val dataSource = QuestLogDatabase.getInstance(activity!!.application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, activity!!.application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)

        // set up toolbar
        val toolbar = dialogView!!.findViewById<androidx.appcompat.widget.Toolbar>(R.id.dialog_toolbar)
        toolbar.title = if(this.skill == null) getString(R.string.add_skill) else getString(R.string.edit_skill)

        if(this.skill != null) {
            toolbar.inflateMenu(R.menu.menu_skill_dialog_actionbar)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete_skill -> {
                        viewModel!!.onDeleteSkill(this.skill)
                        dismiss() // close the dialog
                    }
                    else -> Log.e(TAG,"onCreateDialog: Some nonexistent action menu item was clicked!")
                }
                true
            }
        }

        dialogView!!.new_skill_name.setClearFocusOnDone()

        // make Dialog
        val acceptMessage:String = if(this.skill == null) getString(R.string.add) else getString(R.string.save)

        return AlertDialog.Builder(activity)
            .setView(dialogView)
            .setPositiveButton(acceptMessage,
                DialogInterface.OnClickListener { _, _ ->
                    if(this.skill == null) {
                        val newSkill = Skill(dialogView!!.new_skill_name.text.toString())
                        viewModel!!.onAddNewSkill(newSkill)
                    }
                    else {
                        this.skill.name = dialogView!!.new_skill_name.text.toString()
                        viewModel!!.onEditSkill(this.skill)
                    }
                })
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show()
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