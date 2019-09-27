package com.application.expertnewdesign.lesson.article

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class VideoFragmentPagerAdapter(private val listVideo: List<String>, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return listVideo.size
    }

    override fun getItem(position: Int): Fragment {
        return VideoFragment(listVideo[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listVideo[position]
    }
}