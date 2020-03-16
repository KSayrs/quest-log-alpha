package com.example.questlogalpha

import android.view.inputmethod.EditorInfo
import android.widget.EditText

/** When the "done" action is checked on the keyboard, clear focus on the text item.
 *
 * Sets the [EditText.setOnEditorActionListener] to clear focus on [EditorInfo.IME_ACTION_DONE].
 *
 * Extension for EditText. */
fun EditText.setClearFocusOnDone()
{
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            this.clearFocus()
        }
        false
    }
}
