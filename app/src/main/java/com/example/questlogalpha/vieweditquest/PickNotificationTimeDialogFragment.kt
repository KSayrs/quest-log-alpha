package com.example.questlogalpha.vieweditquest

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.questlogalpha.NotificationUtil
import com.example.questlogalpha.R
import com.example.questlogalpha.Util
import com.example.questlogalpha.databinding.DialogFragmentPickNotificationTimeBinding
import java.security.acl.Group
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class PickNotificationTimeDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {

    var questDueDate: ZonedDateTime? = null

    private var bind: DialogFragmentPickNotificationTimeBinding? = null

    private var lastClicked: View? = null
    private var lastClickedGroup: Group = Group.None
    private var chosenTime: Long = 0L

    /** quantity of some type (days, hours, etc.) before the due date */
    private var number = 0

    /** some type (days, hours, etc.) before the due date */
    private var type = TimeType.Minutes

    enum class Group {
        None,
        TimeDue,
        Spinner,
        CustomTime
    }

    var onSetCustomTime: (() -> Unit)? = null
    var onPositiveButtonClicked: ((timeInMillis: Long) -> Unit)? = null

    // override
    // -----------------------------------------------------------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogFragmentPickNotificationTimeBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_fragment_pick_notification_time, null, false)
        bind = binding

        // set up spinner
        val intervalSpinner: Spinner = binding.intervalSpinner
        intervalSpinner.adapter = ArrayAdapter<TimeType>(context!!, android.R.layout.simple_spinner_item, TimeType.values())
        intervalSpinner.onItemSelectedListener = this

        // todo replace the values for this spinner depending on which interval is selected
        val incrementSpinner: Spinner = binding.incrementSpinner
        incrementSpinner.adapter = ArrayAdapter.createFromResource(context!!, R.array.minutes_array, android.R.layout.simple_spinner_item)
        incrementSpinner.onItemSelectedListener = this

        binding.toggleGroup.setOnClickListener {
            changeBackgroundColor(it)
            lastClickedGroup = Group.TimeDue
        }
        binding.spinnerGroup.setOnClickListener {
            changeBackgroundColor(it)
            lastClickedGroup = Group.Spinner
        }
        binding.customTimeGroup.setOnClickListener {
            setCustomTime()
            changeBackgroundColor(it)
            lastClickedGroup = Group.CustomTime
        }

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setPositiveButton(getString(R.string.add),
                DialogInterface.OnClickListener { _, _ ->
                    when(lastClickedGroup){
                        PickNotificationTimeDialogFragment.Group.CustomTime -> {
                            onPositiveButtonClicked?.invoke(chosenTime)
                        }
                        PickNotificationTimeDialogFragment.Group.TimeDue -> {

                            Log.d(TAG, "chosen time: ${questDueDate!!.toInstant().toEpochMilli()} | System: ${System.currentTimeMillis()}")
                            val plusOffset = questDueDate!!.plusMonths(1L)
                            onPositiveButtonClicked?.invoke(plusOffset!!.toInstant().toEpochMilli())
                        }
                        PickNotificationTimeDialogFragment.Group.Spinner -> {
                            onPositiveButtonClicked?.invoke(chosenTime)
                        }
                        PickNotificationTimeDialogFragment.Group.None -> {
                            Util.showShortToast(context!!, "No option selected")
                            // do nothing
                        }
                    }
                })
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Util.showShortToast(context!!, "negative")
                })
            .create()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // required
        Log.d(TAG, "onNothingSelected()")
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        when(parent.id) {
            bind!!.incrementSpinner.id -> { number = parent.getItemAtPosition(position).toString().toInt() }
            bind!!.intervalSpinner.id -> { type = parent.getItemAtPosition(position) as TimeType }
            else -> { Util.showShortToast(context!!, "other spinner must have been tapped") }
        }

        if(questDueDate != null) {
            var notificationDueDate:ZonedDateTime = questDueDate!!
            Log.d(TAG, "quest Due Date: $questDueDate | now: ${ZonedDateTime.now()}")

            when(type) {
               TimeType.Minutes -> { notificationDueDate = notificationDueDate.minusMinutes(number.toLong()) }
               TimeType.Hours   -> { notificationDueDate = notificationDueDate.minusHours(number.toLong())   }
               TimeType.Days    -> { notificationDueDate = notificationDueDate.minusDays(number.toLong())    }
               TimeType.Weeks   -> { notificationDueDate = notificationDueDate.minusWeeks(number.toLong())   }
               TimeType.Months  -> {
                   notificationDueDate = notificationDueDate.minusMonths(number.toLong())
               }
               TimeType.Years   -> { notificationDueDate = notificationDueDate.minusYears(number.toLong())   }
            }

            notificationDueDate = notificationDueDate.plusMonths(1) // handling conversion offset
            chosenTime = notificationDueDate.toInstant().toEpochMilli()
            Util.showShortToast(context!!, "chosen Time: $chosenTime | current: ${System.currentTimeMillis()}")
        }
        else { Util.showShortToast(context!!, "questDueDate is null!!") }
        Log.d(TAG, "chosen time: $chosenTime | System: ${System.currentTimeMillis()} | Quest: ${questDueDate!!.toInstant().toEpochMilli()} | System 2: ${ZonedDateTime.now().toInstant().toEpochMilli()}")
        Log.d(TAG, "Number: $number | TimeType: $type")
        changeBackgroundColor(bind!!.spinnerGroup)
        lastClickedGroup = Group.Spinner
    }

    // private
    // -----------------------------------------------------------------------------------------------------------------------

    private fun changeBackgroundColor(view: View) {
        if(lastClicked != null) lastClicked!!.background = ColorDrawable(Color.TRANSPARENT)
        view.background = ColorDrawable(resources.getColor(R.color.highlightColor, null))
        lastClicked = view
    }

    private fun setCustomTime() {
       // onSetCustomTime?.invoke()
        setCustomText()
    }

    private fun setCustomText() {
        val dialog = DatePickerDialogFragment()
        val timeDialog = TimePickerDialogFragment(true)

        val alarm: Calendar = Calendar.getInstance()

        dialog.onDateSet = { year, month, day ->
            Log.d(TAG, "setCustomText: Date picked: $year $month $day")
            alarm.add(Calendar.YEAR, (year - alarm.get(Calendar.YEAR)))
            alarm.add(Calendar.MONTH, (month - alarm.get(Calendar.MONTH)))
            alarm.add(Calendar.DATE, (day - alarm.get(Calendar.DATE)))

            timeDialog.show(childFragmentManager, "addTime")
        }

        // don't check for the past because sometimes we want to purposefully acknowledge a quest is overdue
        timeDialog.onTimeSet = { hour, minute ->
            alarm.add(Calendar.HOUR_OF_DAY, (hour - alarm.get(Calendar.HOUR_OF_DAY)))
            alarm.add(Calendar.MINUTE, (minute - alarm.get(Calendar.MINUTE)))

            chosenTime = alarm.timeInMillis

            Log.d(TAG, "alarm in millis: $chosenTime | System: ${System.currentTimeMillis()}")
            val millisToZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(alarm.timeInMillis), ZoneId.systemDefault())

            bind!!.customTime.text = NotificationUtil.formatNotificationText(questDueDate!!, millisToZoned, true)
        }

        dialog.show(childFragmentManager, "whatever")
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: PickNotificationTimeDialogFragment"
    }
}