package com.example.questlogalpha.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.questlogalpha.R
import com.example.questlogalpha.databinding.FragmentMainViewBinding
import com.google.android.material.snackbar.Snackbar

class MainViewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentMainViewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_view, container, false)

        binding.lifecycleOwner = this

        // left/right scrolling. This activity probably won't actually need anything else.
        val sectionsPagerAdapter = SectionsPagerAdapter(activity!!.applicationContext, activity!!.supportFragmentManager) //todo is this right?
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        // other things in the main activity
        binding.fab.setOnClickListener { view: View ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        return binding.root
    }
}