package com.example.questlogalpha

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.questlogalpha.ui.main.MainViewFragmentDirections
import com.example.questlogalpha.widget.QuestLogWidgetRemoteViewsFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar_main))
        if(actionBar != null) {
            actionBar?.hide()
        }
        if(supportActionBar == null){
            Log.e("$TAG: onCreate: ", "supportActionBar is null.")
        }
        else {
            supportActionBar?.hide()
        }
    }

    // Hide the navigation bar when we're on this activity
    override fun onResume() {
        super.onResume()
        Log.d("$TAG: onResume", " called")

        if(supportActionBar == null){
            Log.e(TAG, "onResume: supportActionBar is null.")
        }
        else {
            if(supportActionBar!!.isShowing) supportActionBar!!.hide()
        }

        if(intent.action == QuestLogWidgetRemoteViewsFactory.ACTION_OPEN_APP) {
            Log.d(TAG, "intent came from widget (onResume)")
            val questId = intent?.extras?.getString("questId")
            if(questId != null) {
                if (this.findNavController(R.id.nav_host_fragment).currentDestination?.id == R.id.mainViewFragment) {
                    this.findNavController(R.id.nav_host_fragment).navigate(
                        MainViewFragmentDirections.actionMainViewFragmentToViewEditQuestFragment(questId)
                    )
                }
                else {
                    Log.e(TAG, "Current destination is " + this.findNavController(R.id.nav_host_fragment).currentDestination?.label + " instead of R.id.mainViewFragment!")
                }
            }
            else {
                Log.e(TAG, "questID is null")
            }
        }
    }

    // This method is called when the up button is pressed. Just mimic the back button for now.
    // The back button takes you back to this activity instead of a fragment, which doesn't have a navController.
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: MainActivity"
    }
}