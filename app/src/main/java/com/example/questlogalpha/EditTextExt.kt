package com.example.questlogalpha

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/** When the "done" action is checked on the keyboard, clear focus on the text item.
 *
 * Sets the [EditText.setOnEditorActionListener] to clear focus on [EditorInfo.IME_ACTION_DONE].
 *
 * Extension for EditText. */
fun EditText.setClearFocusOnDone() {
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            this.clearFocus()
        }
        false
    }
}

fun EditText.focus(application: Application) {
    this.isFocusableInTouchMode = true
    this.requestFocus()
    val imm = application.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}
