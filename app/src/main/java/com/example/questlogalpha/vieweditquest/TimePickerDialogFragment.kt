package com.example.questlogalpha.vieweditquest

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.questlogalpha.R

/** [TimePickerDialog] that defaults to the current time. Used for setting the time for familiar reminders. */
class TimePickerDialogFragment(private val followsDatePicker: Boolean = false) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    /** Do something with the time chosen by the user. */
    var onTimeSet: ((hour: Int, minute: Int) -> Unit)? = null

    /** Do something just before the dialog calls onDismiss. */
    var onDismiss: (() -> Unit)? = null

    override fun onDismiss(dialog: DialogInterface) {
        onDismiss?.invoke()
        super.onDismiss(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        val dialog = TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        if(followsDatePicker) dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.skip), dialog)
        return dialog
    }

    // Do something with the time chosen by the user
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        onTimeSet?.invoke(hourOfDay, minute)
    }
}