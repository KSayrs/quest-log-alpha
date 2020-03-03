package com.example.questlogalpha.skills

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.data.Skill
import com.example.questlogalpha.databinding.SkillItemViewBinding
import kotlinx.android.synthetic.main.skill_item_view.view.*

class SkillItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

class SkillsAdapter: RecyclerView.Adapter<SkillItemViewHolder>() {
    var chosenSkill:Skill? = null // used for dialogs

    var data = listOf<Skill>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    // this happens after onCreate
    override fun onBindViewHolder(holder: SkillItemViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding <SkillItemViewBinding>(holder.itemView)
        binding?.position = position
        binding?.skill = data[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: SkillItemViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.skill_item_view, parent, false)
        val holder = SkillItemViewHolder(binding.root.skill_item_view_layout)

        binding.root.setOnClickListener {
            Log.d(TAG,"item ${holder.adapterPosition} tapped")
            chosenSkill = binding.skill
        }

        return holder
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: SkillsAdapter"
    }
}