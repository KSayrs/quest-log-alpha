package com.example.questlogalpha.quests

import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.data.Quest
import kotlinx.android.synthetic.main.quest_item_view.view.*
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.example.questlogalpha.R
import com.example.questlogalpha.data.SkillReward
import com.example.questlogalpha.databinding.QuestItemViewBinding
import com.example.questlogalpha.skills.SkillsViewModel
import kotlinx.android.synthetic.main.icon_item_view.view.*

/**
 * ViewHolder that holds a single [ConstraintLayout].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 *
 * // todo implement this a different way, maybe with an interface and/or a separate handler for the on-click so that the adapter doesn't require a reference to the viewModel.
 */
class QuestItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

/** ************************************************************************************************
 * A [RecyclerView.Adapter] for quests. Shows quest image, title, and description, as well as the option to edit or remove them.
 * ********************************************************************************************** */
class QuestsAdapter(private val childFragmentManager: FragmentManager): RecyclerView.Adapter<QuestItemViewHolder>() {
    var data = listOf<Quest>()
        set(value) {

            val filteredList = mutableListOf<Quest>()

            if(viewModel != null) {
                if (viewModel!!.viewingCompleted.value!!) {
                    for (item in value) {
                        if (item.completed) filteredList.add(item)
                    }
                }
                else {
                    for (item in value) {
                        if (!item.completed) filteredList.add(item)
                    }
                }

                field = filteredList
            }
            else field = value
            notifyDataSetChanged()
        }

    var viewModel: QuestsViewModel? = null

    var onQuestTitleTapped: ((Quest) -> Unit)? = null

    override fun getItemCount() = data.size

    // this happens after onCreate, so the data is not null anymore
    override fun onBindViewHolder(holder: QuestItemViewHolder, position: Int) {
        val item = data[position]
        holder.constraintLayout.quest_title.text = item.title
        holder.constraintLayout.quest_description.text = item.description
        if(item.icon != 0) {
            holder.constraintLayout.quest_icon.setImageResource(item.icon)
        }

        // set up condition-based styling
        // since incomplete is the default, we only need to restyle for completed quests
        if(item.completed) {
            styleComplete(holder.constraintLayout.quest_title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: QuestItemViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.quest_item_view, parent, false)
        val holder = QuestItemViewHolder(binding.root.quest_item_view_layout)

        // set click handlers
        binding.deleteQuestIcon.setOnClickListener {
            val pos = holder.adapterPosition
            if(viewModel != null){
                viewModel!!.onDeleteQuest(data[pos].id)
            }
            else Log.e(TAG, "binding.questsViewModel is null!")
        }

        binding.editQuestIcon.setOnClickListener {
            val pos = holder.adapterPosition
            if(viewModel != null){
                viewModel!!.onQuestEdit(data[pos])
            }
            else Log.e(TAG, "binding.questsViewModel is null!")
        }

        val oldColors: ColorStateList = binding.questTitle.textColors
        binding.questTitle.setOnClickListener {
            val pos = holder.adapterPosition
            onQuestTitleTapped?.invoke(data[pos])

            if(data[pos].completed){
                binding.questTitle.setTextColor(oldColors)
                binding.questTitle.paintFlags = (binding.questTitle.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv()))
            }
            else {
                styleComplete(binding.questTitle)
            }
        }

        binding.questIcon.setOnClickListener {
            val pos = holder.adapterPosition
            val dialog = ChooseIconDialogFragment()

            // man this is silly
            dialog.onIconTapped = {icon ->
                viewModel?.onSetQuestIcon(data[pos].id, icon)
                binding.questIcon.setImageResource(icon)
                dialog.dismiss()
            }

            dialog.show(childFragmentManager, "chooseIcon")
        }

        return holder
    }

    // ----------------------- styling functions ------------------- //
    private fun styleComplete(view: TextView){
        animateStrikeThrough1(view)
       // view.paintFlags = (view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
        view.setTextColor(ContextCompat.getColor(view.context, R.color.colorTertiary))
    }

    private fun animateStrikeThrough1(tv: TextView) {
        val ANIM_DURATION:Long = 250 //duration of animation in millis
        val length = tv.text.length
        object: CountDownTimer(ANIM_DURATION, ANIM_DURATION/length) {

            val span: Spannable = SpannableString(tv.text)
            val strikethroughSpan:StrikethroughSpan = StrikethroughSpan()

            override fun onTick(millisUntilFinished:Long){
                //calculate end position of strikethrough in textview
                var endPosition:Int = (((millisUntilFinished-ANIM_DURATION)*-1)/(ANIM_DURATION/length)).toInt() + 1
                if(endPosition > length) endPosition = length
                span.setSpan(strikethroughSpan, 0, endPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                tv.text = span
            }

            override fun onFinish(){}
        }.start()
    }

    private fun removeStrikeThough1(tv: TextView) {
        // todo implement removeStrikeThough1
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: QuestsAdapter"
    }
}