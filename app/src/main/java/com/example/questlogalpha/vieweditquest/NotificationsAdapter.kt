package com.example.questlogalpha.vieweditquest

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.notifications.NotificationUtil
import com.example.questlogalpha.R
import com.example.questlogalpha.data.StoredNotification
import com.example.questlogalpha.databinding.QuestFamiliarNotificationViewBinding
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * ViewHolder that holds a single [ConstraintLayout].
 *
 * A ViewHolder holds a view for the [RecyclerView] as well as providing additional information
 * to the RecyclerView such as where on the screen it was last drawn during scrolling.
 */
class NotificationItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

/** ************************************************************************************************
 * A [RecyclerView.Adapter] to show all skill rewards for a given quest, as well as the option to remove them.
 * ********************************************************************************************** */
class NotificationsAdapter: RecyclerView.Adapter<NotificationItemViewHolder>() {
    var data = listOf<StoredNotification>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemRemoved: ((StoredNotification) -> Unit)? = null
    var questDueDate: ZonedDateTime? = null

    private var _binding: QuestFamiliarNotificationViewBinding? = null
    private val binding get() = _binding!!

    // override
    // ---------------------------------------------------------------------------------------------

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: NotificationItemViewHolder, position: Int) {
        val item = data[position]
        if(questDueDate == null) Log.e(TAG, "onBindViewHolder: questDueDate is null!!!")
        else {
            Log.d(TAG, "Instant. ofEpochMilli(item.notificationTime): ${Instant.ofEpochMilli(item.notificationTime).atZone(ZoneId.systemDefault())}")
            val millisToZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(item.notificationTime), ZoneId.systemDefault())
            binding.alertTimeText.text = NotificationUtil.formatNotificationText(questDueDate!!, millisToZoned, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        _binding = DataBindingUtil.inflate(layoutInflater, R.layout.quest_familiar_notification_view, parent, false)
        val holder = NotificationItemViewHolder(binding.familiarNotificationInQuest)

        binding.deleteAlertIcon.setOnClickListener {
            val pos = holder.adapterPosition

            onItemRemoved?.invoke(data[pos])
            remove(pos)
        }

        return holder
    }

    // private
    // ---------------------------------------------------------------------------------------------

    fun remove(position: Int){
        notifyItemChanged(position)
        notifyItemRangeRemoved(position, 1)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: NotificationsAdapter"
    }
}