package com.example.questlogalpha.quests

import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.Util
import com.example.questlogalpha.Util.Companion.styleComplete
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.databinding.QuestItemViewBinding

/**
 * ViewHolder that holds a single [ConstraintLayout].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 *
 * // todo implement this a different way, maybe with an interface and/or a separate handler for the on-click so that the adapter doesn't require a reference to the viewModel.
 */
class QuestItemViewHolder(private val constraintLayout: ConstraintLayout,
                          private val binding: QuestItemViewBinding,
                          private val onQuestTitleTapped: ((Quest) -> Unit)?,
                          private val viewModel: QuestsViewModel?,
                          private val childFragmentManager: FragmentManager): RecyclerView.ViewHolder(constraintLayout) {

    fun bind(quest: Quest) {
        binding.questTitle.text = quest.title
        binding.questDescription.text = quest.description
        if(quest.icon != 0) {
            binding.questIcon.setImageResource(quest.icon)
        }

        // set up condition-based styling
        // since incomplete is the default, we only need to restyle for completed quests
        if(quest.completed) {
            styleComplete(binding.questTitle)
        }

        binding.questTitle.setOnClickListener {
            onQuestTitleTapped?.invoke(quest)

            Log.d("KSLOG", "quest title tapped ${binding.questTitle.text}")

            if(!quest.completed){
                Util.styleComplete(binding.questTitle)
            }
            else {
                binding.questTitle.setTextColor(Color.DKGRAY)
                binding.questTitle.paintFlags = (binding.questTitle.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv()))
            }
        }

        binding.questIcon.setOnClickListener {
            val dialog = ChooseIconDialogFragment()

            // man this is silly
            dialog.onIconTapped = { icon ->
                viewModel?.onSetQuestIcon(quest.id, icon)
                binding.questIcon.setImageResource(icon)
                dialog.dismiss()
            }

            dialog.show(childFragmentManager, "chooseIcon")
        }
    }
}

/** ************************************************************************************************
 * A [RecyclerView.Adapter] for quests. Shows quest image, title, and description, as well as the option to edit or remove them.
 * ********************************************************************************************** */
class QuestsAdapter(private val childFragmentManager: FragmentManager): RecyclerView.Adapter<QuestItemViewHolder>() {
    var data = listOf<Quest>()
        set(value) {

            val filteredList = mutableListOf<Quest>()

        //  if(viewModel != null) {
        //      if (viewModel!!.viewingCompleted.value!!) {
        //          for (item in value) {
        //              if (item.completed) filteredList.add(item)
        //          }
        //      }
        //      else {
        //          for (item in value) {
        //              if (!item.completed) filteredList.add(item)
        //          }
        //      }

        //      field = filteredList
        //  }
         //   else field = value
            field = value
            notifyDataSetChanged()
        }

    var viewModel: QuestsViewModel? = null

    var onQuestTitleTapped: ((Quest) -> Unit)? = null

    private var _binding: QuestItemViewBinding? = null
    private val binding get() = _binding!!

    override fun getItemCount() = data.size

    // this happens after onCreate, so the data is not null anymore
    override fun onBindViewHolder(holder: QuestItemViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        _binding = DataBindingUtil.inflate(layoutInflater, R.layout.quest_item_view, parent, false)
        val holder = QuestItemViewHolder(binding.questItemViewLayout, binding, onQuestTitleTapped, viewModel, childFragmentManager)

        // set click handlers
        binding.deleteQuestIcon.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if(viewModel != null){
                viewModel!!.onDeleteQuest(data[pos].id)
            }
            else Log.e(TAG, "binding.questsViewModel is null!")
        }

        binding.editQuestIcon.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if(viewModel != null){
                viewModel!!.onQuestEdit(data[pos])
            }
            else Log.e(TAG, "binding.questsViewModel is null!")
        }

        return holder
    }

    private fun removeStrikeThough1(tv: TextView) {
        // todo implement removeStrikeThough1
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsAdapter"
    }
}