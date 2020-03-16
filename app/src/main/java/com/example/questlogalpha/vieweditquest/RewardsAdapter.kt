// todo in rewrite - make general adapter that handles more of the boilerplate
package com.example.questlogalpha.vieweditquest

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.data.SkillReward
import com.example.questlogalpha.databinding.SkillInRewardListBinding
import kotlinx.android.synthetic.main.skill_in_reward_list.view.*

/**
 * ViewHolder that holds a single [ConstraintLayout].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 *
 * // todo move this class elsewhere
 */
class RewardItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

class RewardsAdapter: RecyclerView.Adapter<RewardItemViewHolder>() {
    var data = listOf<SkillReward>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var viewModel: ViewEditQuestViewModel? = null

    var onItemRemoved: ((SkillReward) -> Unit)? = null

    override fun getItemCount() = data.size

    // this happens after onCreate
    override fun onBindViewHolder(holder: RewardItemViewHolder, position: Int) {
        val item = data[position]
        holder.constraintLayout.skill_name.text = holder.constraintLayout.context.getString(R.string.skill_reward_amount, String.format("%.0f", item.amount), item.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: SkillInRewardListBinding = DataBindingUtil.inflate(layoutInflater, R.layout.skill_in_reward_list, parent, false)
        val holder = RewardItemViewHolder(binding.root.skill_in_reward_list_layout)

        binding.removeSkillRewardIcon.setOnClickListener {
            val pos = holder.adapterPosition

            onItemRemoved?.invoke(data[pos])
            remove(pos)
        }

        return holder
    }

    private fun remove(position: Int){
        notifyItemChanged(position)
        notifyItemRangeRemoved(position, 1)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: RewardsAdapter"
    }
}