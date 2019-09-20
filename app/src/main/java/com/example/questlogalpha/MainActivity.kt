package com.example.questlogalpha

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar_main))
        if(actionBar != null)
        {
            actionBar?.hide()
        }
        if(supportActionBar == null){
            Log.e("MainActivity.kt: onCreate: ", "supportActionBar is null.")
        }
        else {
            supportActionBar?.hide()
        }
    }

    // Hide the navigation bar when we're on this activity
    override fun onResume() {
        super.onResume()
        Log.d("MainActivity.kt: onResume", " called")

        if(supportActionBar == null){
            Log.e("MainActivity.kt: onResume: ", "supportActionBar is null.")
        }
        else {
            if(supportActionBar!!.isShowing) supportActionBar!!.hide()
        }
    }

    // This method is called when the up button is pressed. Just mimic the back button for now.
    // The back button takes you back to this activity instead of a fragment, which doesn't have a navController.
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}