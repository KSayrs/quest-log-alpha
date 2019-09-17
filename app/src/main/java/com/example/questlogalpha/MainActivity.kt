package com.example.questlogalpha

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


// todo change toolbar styling in xml file
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
}