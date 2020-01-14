// todo might not be needed. was going to be for the dialogfragment, but we may actually just be able tp reuse the one

package com.example.questlogalpha.skills


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.data.Skill
import androidx.recyclerview.widget.ListAdapter
import com.example.questlogalpha.R
import com.example.questlogalpha.databinding.SkillItemViewBinding
import kotlinx.android.synthetic.main.skill_item_view.view.*

// todo change this to a ListAdapter, and make a separate recyclerview adapter for the actual skills screen
class SkillListItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

class SkillsListAdapter : ListAdapter<Skill, SkillListItemViewHolder>(SkillDC()) {
    var data = listOf<Skill>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: SkillItemViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.skill_item_view, parent, false)
        val holder = SkillListItemViewHolder(binding.root.skill_item_view_layout)

        binding.root.setOnClickListener {
            // todo select this one
            Log.d(TAG,"item ${holder.adapterPosition} tapped")
        }

        Log.d(TAG,"onCreateViewHolder")

        return holder
    }

    override fun onBindViewHolder(holder: SkillListItemViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding <SkillItemViewBinding>(holder.itemView)
        binding?.position = position
        binding?.skill = data[position]
    }

    // taken from https://github.com/karntrehan/Posts/blob/2461516e514b41c44af5feddc2df21179d729eb3/posts/src/main/java/com/karntrehan/posts/details/DetailsAdapter.kt#L14
    private class SkillDC : DiffUtil.ItemCallback<Skill>() {
        override fun areItemsTheSame(
            oldItem: Skill,
            newItem: Skill
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Skill,
            newItem: Skill
        ) = oldItem == newItem
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: SkillsListAdapter"
    }
}