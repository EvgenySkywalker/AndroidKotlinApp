package com.application.expertnewdesign.lesson.article

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class VideoFragmentPagerAdapter(private val listVideo: List<String>, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var fragmentsList = emptyList<VideoFragment>().toMutableList()

    override fun getCount(): Int {
        return listVideo.size
    }

    override fun getItem(position: Int): Fragment {
        val newFragment = VideoFragment(listVideo[position])
        fragmentsList.add(newFragment)
        return newFragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listVideo[position]
    }
}