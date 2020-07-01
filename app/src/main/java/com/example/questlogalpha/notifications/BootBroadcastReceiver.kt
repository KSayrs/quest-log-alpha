package com.example.questlogalpha.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootBroadcastReceiver : BroadcastReceiver() {

    /** Currently triggers when the device boots */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        // TODO: implement onReceive
        // get stored alarms
        // re-set all the alarms
        // send a (debug) notification to alert the user that the alarms have been reset
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        var TAG: String = "KSLOG: BootBroadcastReceiver"
    }
}