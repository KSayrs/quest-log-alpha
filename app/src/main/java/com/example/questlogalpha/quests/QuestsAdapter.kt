package com.example.questlogalpha.quests

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.data.Quest

/**
 * ViewHolder that holds a single [TextView].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 *
 * // todo move this class elsewhere
 */
class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)

class QuestsAdapter: RecyclerView.Adapter<TextItemViewHolder>() {
    var data = listOf<Quest>()

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = data[position]
        holder.textView.text = item.title
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        var view = layoutInflater.inflate(R.layout.text_item_view, parent, false) as TextView
        return TextItemViewHolder(view)
    }

}