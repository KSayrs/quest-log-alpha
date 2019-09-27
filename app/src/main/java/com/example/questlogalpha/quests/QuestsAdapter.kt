package com.example.questlogalpha.quests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.data.Quest
import kotlinx.android.synthetic.main.quest_item_view.view.*
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.questlogalpha.R
import com.example.questlogalpha.databinding.QuestItemViewBinding

/**
 * ViewHolder that holds a single [ConstraintLayout].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 *
 * // todo move this class elsewhere
 * // todo implement this a different way, maybe with an interface and/or a separate handler for the on-click so that the adapter doesn't require a reference to the viewModel.
 */
class QuestItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

class QuestsAdapter: RecyclerView.Adapter<QuestItemViewHolder>() {
    var data = listOf<Quest>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var viewModel: QuestsViewModel? = null

    override fun getItemCount() = data.size

    // in theory this happens after onCreate
    override fun onBindViewHolder(holder: QuestItemViewHolder, position: Int) {
        val item = data[position]
        holder.constraintLayout.quest_title.text = item.title
        holder.constraintLayout.quest_description.text = item.description
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: QuestItemViewBinding = DataBindingUtil.inflate(layoutInflater,
            R.layout.quest_item_view, parent, false)
        val holder =
            QuestItemViewHolder(binding.root.quest_item_view_layout)

        binding.position = holder.adapterPosition

        binding.deleteQuestIcon.setOnClickListener {
            val pos = holder.adapterPosition

            Toast.makeText(parent.context, "Adapter position: $pos", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onCreateViewHolder: position: $pos")
            if(viewModel != null){
                viewModel!!.onDeleteQuest(data[pos].id)
            }
            else Log.e(TAG, "binding.questsViewModel is null!")
        }

        binding.editQuestIcon.setOnClickListener {
            val pos = holder.adapterPosition

            Toast.makeText(parent.context, "Adapter position: $pos", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onCreateViewHolder: position: $pos")
            if(viewModel != null){
                viewModel!!.onQuestEdit(data[pos])
            }
            else Log.e(TAG, "binding.questsViewModel is null!")
        }

        return holder
    }

    // shouldn't this be used? It seems to be updating without it though...
    fun remove(position: Int) {
        notifyItemChanged(position)
        notifyItemRangeRemoved(position, 1)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsAdapter"
    }
}