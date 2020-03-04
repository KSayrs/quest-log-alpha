package com.example.questlogalpha

import android.view.inputmethod.EditorInfo
import android.widget.EditText

fun EditText.setClearFocusOnDone()
{
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            this.clearFocus()
        }
        false
    }
}
