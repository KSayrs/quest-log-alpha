package com.example.questlogalpha.skills


import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.data.Skill
import androidx.recyclerview.widget.ListAdapter

// todo change this to a ListAdapter, and make a separate recyclerview adapter for the actual skills screen
class SkillListItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

class SkillsListAdapter : ListAdapter<Skill, SkillListItemViewHolder>(SkillDC()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillListItemViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: SkillListItemViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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