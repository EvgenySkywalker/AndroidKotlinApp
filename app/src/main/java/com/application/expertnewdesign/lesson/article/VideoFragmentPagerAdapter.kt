package com.application.expertnewdesign.lesson.article

import android.content.pm.ActivityInfo
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager


class VideoFragmentPagerAdapter(var pager: ViewPager, private val listVideo: List<String>, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var portrait = 0
    var fragmentsList = emptyList<VideoFragment>().toMutableList()

    override fun getCount(): Int {
        return listVideo.size
    }

    override fun getItem(position: Int): Fragment {
        val newFragment = VideoFragment(pager, listVideo[position])
        fragmentsList.add(newFragment)
        return newFragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listVideo[position]
    }
}