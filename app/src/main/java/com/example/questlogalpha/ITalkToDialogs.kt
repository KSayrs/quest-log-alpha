package com.example.questlogalpha

interface ITalkToDialogs {
    /** When the positive button on the dialog is clicked
     * @param view - this should be the dialog's view, or null if it is not needed
    */
    fun onPositiveButtonClicked(dictionary: Map<String, Any>)
}
