package com.example.questlogalpha

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.data.Quest
import kotlinx.android.synthetic.main.quest_item_view.view.*

/**
 * ViewHolder that holds a single [ConstraintLayout].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 *
 * // todo move this class elsewhere
 */
class QuestItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout){}

class QuestsAdapter: RecyclerView.Adapter<QuestItemViewHolder>() {
    var data = listOf<Quest>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: QuestItemViewHolder, position: Int) {
        val item = data[position]
        holder.constraintLayout.quest_title.text = item.title
        holder.constraintLayout.quest_description.text = item.description
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.quest_item_view, parent, false) as ConstraintLayout
        return QuestItemViewHolder(view)
    }
}