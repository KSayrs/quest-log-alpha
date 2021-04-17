package com.example.questlogalpha.skills

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.databinding.DialogFragmentAddEditSkillBinding
import com.example.questlogalpha.databinding.SkillItemViewBinding

class SkillItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

/** ************************************************************************************************
 * A [RecyclerView.Adapter] to show all skills in the database.
 * ********************************************************************************************** */
class SkillsAdapter: RecyclerView.Adapter<SkillItemViewHolder>() {
    var chosenSkill:Skill? = null // used for dialogs
    var onItemClick: ((Skill, View) -> Unit)? = null
    var onSelectionChange: ((View) -> Unit)? = null
    var onViewAttached: ((View) -> Unit)? = null

    private var lastClicked:View? = null

    private var _binding: SkillItemViewBinding? = null
    private val binding get() = _binding!!

    var data = listOf<Skill>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // override --------------------------------------------------------------------------------

    override fun getItemCount() = data.size

    // this happens after onCreate
    override fun onBindViewHolder(holder: SkillItemViewHolder, position: Int) {
        _binding = DataBindingUtil.getBinding <SkillItemViewBinding>(holder.itemView)
        binding.position = position
        binding.skill = data[position]
        onViewAttached?.invoke(binding.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: SkillItemViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.skill_item_view, parent, false)
        val holder = SkillItemViewHolder(binding.skillItemViewLayout)

        binding.root.setOnClickListener {
            Log.d(TAG,"item ${holder.adapterPosition} tapped")
            chosenSkill = binding.skill
            onItemClick?.invoke(binding.skill!!, holder.itemView)

            handleSelectionChange(it)
        }

        return holder
    }

    // public --------------------------------------------------------------------------------

    /** Manually set [lastClicked] to a new view. */
    fun overrideLastClicked(view: View) {
        handleSelectionChange(view)
    }

    // private --------------------------------------------------------------------------------

    private fun handleSelectionChange(view: View) {
        if(lastClicked == null) lastClicked = view
        else if(lastClicked != view) {
            onSelectionChange?.invoke(lastClicked!!)
            lastClicked = view
        }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: SkillsAdapter"
    }
}