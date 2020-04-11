package com.example.questlogalpha.vieweditquest

import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.data.StoredNotification
import com.example.questlogalpha.databinding.QuestFamiliarNotificationViewBinding
import kotlinx.android.synthetic.main.quest_familiar_notification_view.view.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.ChronoZonedDateTime
import kotlin.math.abs

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

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: NotificationItemViewHolder, position: Int) {
        val item = data[position]
        if(questDueDate == null) Log.e(TAG, "onBindViewHolder: questDueDate is null!!!")
        else {
            Log.d(TAG, "Instant.ofEpochMilli(item.notificationTime): ${Instant.ofEpochMilli(item.notificationTime)}")
            val millisToZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(item.notificationTime), ZoneId.systemDefault())
            holder.constraintLayout.alert_time_text.text = formatNotificationText(questDueDate!!, millisToZoned)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: QuestFamiliarNotificationViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.quest_familiar_notification_view, parent, false)
        val holder = NotificationItemViewHolder(binding.root.familiar_notification_in_quest)

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

    /** Formats the display string for the reminder time. */
    private fun formatNotificationText(dueDate: ZonedDateTime, notificationTime: ZonedDateTime): String {

        Log.d(TAG, "formatNotificationText: dueDate: $dueDate")
        Log.d(TAG, "formatNotificationText: notificationTime: $notificationTime")

        // -1 offsets because something happens in the millisecond conversion
        val years = dueDate.year - notificationTime.year
        val months = dueDate.monthValue - (notificationTime.monthValue - 1)
        val days = dueDate.dayOfMonth - (notificationTime.dayOfMonth)
        val hours = dueDate.hour - (notificationTime.hour)
        val minutes = dueDate.minute - (notificationTime.minute)

        return buildText(years, months, days, hours, minutes)
    }

    /** Builds the display string for the notification time. */
    private fun buildText(years: Int, months: Int, days: Int, hours: Int, minutes: Int): String {
        var text = ""

        if(years != 0) {
            text += "${abs(years)}y "
        }
        if(months != 0) {
            text += "${abs(months)}m "
        }
        if(days != 0) {
            text += "${abs(days)}d "
        }
        if(hours != 0) {
            text += "${abs(hours)}h "
        }
        if(minutes != 0) {
            text += "${abs(minutes)}m "
        }

        text += " before"

      //  if(text.length > 8) {
      //      val substrings = text.split(" ")
      //      var currentLength = 0
      //      for(substring in substrings) {
      //          if(substring.length + currentLength + 1 <= 8) {  // +1 for the space at the end that's removed for delimiting
      //              currentLength += substring.length + 1;
      //          } else {
      //              return text.substring(0, currentLength)
      //          }
      //      }
      //      return text
      //  }

        return text
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: NotificationsAdapter"
    }
}