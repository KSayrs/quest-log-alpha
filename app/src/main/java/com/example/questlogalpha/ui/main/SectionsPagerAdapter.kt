package com.example.questlogalpha.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.questlogalpha.R
import com.example.questlogalpha.quests.QuestsFragment
import com.example.questlogalpha.skills.SkillsFragment

private val TAB_TITLES = arrayOf(
    R.string.timeline,
    R.string.quests,
    R.string.personnage
)

/** ************************************************************************************************
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * ********************************************************************************************** */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, private val toolbar: androidx.appcompat.widget.Toolbar) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PlaceholderFragment.newInstance(position + 1 )
            1 -> QuestsFragment(toolbar)
            2 -> SkillsFragment()
            else -> {
                PlaceholderFragment.newInstance(position + 1 )
            }
        }
    }

    // todo get from activity_main.xml layout file? Or handle in activity?
    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show as many total pages as there are tabs.
        return TAB_TITLES.size
    }
}