package com.origin.moreads.ui.activities.onboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    shouldShowFragment3: Boolean
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = buildList {
        add(Fragment1())
        add(Fragment2())
        if (shouldShowFragment3) add(Fragment3())
        add(Fragment4())
        add(Fragment5())
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
