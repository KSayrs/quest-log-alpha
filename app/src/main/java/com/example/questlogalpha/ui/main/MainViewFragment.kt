package com.example.questlogalpha.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.questlogalpha.R
import com.example.questlogalpha.databinding.FragmentMainViewBinding

/** ************************************************************************************************
 * The main [Fragment] that shows the viewPager.
 * ********************************************************************************************** */
class MainViewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentMainViewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_view, container, false)

        binding.lifecycleOwner = this

        val toolbar = binding.title

        // left/right scrolling. This fragment probably won't actually need anything else.
        binding.viewPager.adapter = SectionsPagerAdapter(activity!!.applicationContext, childFragmentManager, toolbar) // so far this one doesn't have the blank back problem
        binding.tabs.setupWithViewPager(binding.viewPager)

        // set the quests list to be the default landing page
        binding.viewPager.currentItem = 1 // todo don't use a hard value

        return binding.root
    }

}