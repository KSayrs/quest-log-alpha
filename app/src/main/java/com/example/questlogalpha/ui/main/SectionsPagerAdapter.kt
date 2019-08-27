package com.example.questlogalpha.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.questlogalpha.R


private val TAB_TITLES = arrayOf(
    R.string.timeline,
    R.string.quests,
    R.string.skills
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            1 -> return PlaceholderFragment.newInstance(position + 1 )
            2 -> return QuestsFragment()
            else -> { return PlaceholderFragment.newInstance(position + 1 )}
        }
    }

    // todo get from activity_main.xml layout file? Or hanle in activity?
    override fun getPageTitle(position: Int): CharSequence? {

        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show as many total pages as there are tabs.
        return TAB_TITLES.size
    }
}