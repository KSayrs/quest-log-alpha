package com.example.questlogalpha.vieweditquest

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment

/** [DatePickerDialog] that defaults to the current time. Used for setting the time for familiar reminders. */
class DatePickerDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    /** Do something with the date chosen by the user. */
    var onDateSet: ((year: Int, month: Int, day: Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) // we can't handle the offset here, we have to do it elsewhere
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        onDateSet?.invoke(year, month, day)
    }
}
