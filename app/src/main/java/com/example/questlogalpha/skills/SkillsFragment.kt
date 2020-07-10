package com.example.questlogalpha.skills

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentSkillsBinding

/** ************************************************************************************************
 * [Fragment] to display all skills in the database.
 * ********************************************************************************************** */
class SkillsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e(TAG, "onCreate: supportActionBar is null.")
        else supportActionBar.hide()
    }

    // Hide the navigation bar when we're on this fragment
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")

        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e("MainActivity.kt: onResume: ", "supportActionBar is null.")
        else if(supportActionBar.isShowing) supportActionBar.hide()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentSkillsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_skills, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).skillsDatabaseDao
        val viewModelFactory = ViewModelFactory("", null, dataSource, null, application =  application)
        val skillsViewModel = ViewModelProvider(this, viewModelFactory).get(SkillsViewModel::class.java)
        val adapter = SkillsAdapter()
        binding.skillList.adapter = adapter

        binding.skillsViewModel = skillsViewModel
        binding.lifecycleOwner = this

        // assign the skill data to the adapter
        skillsViewModel.skills.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        // add skill button
        binding.addSkillButton.setOnClickListener{
            val dialog = AddEditSkillDialogFragment(null)
            dialog.show(childFragmentManager, "addSkill")
        }

        adapter.onItemClick = { skill, _ ->
            val dialog = AddEditSkillDialogFragment(skill)
            dialog.show(childFragmentManager, "editSkill")
        }

        return binding.root
    }

    // -------------------------- log tag ------------------------------ //

    companion object {
        const val TAG: String = "KSLOG: SkillsFragment"
    }
}