package com.application.expertnewdesign.lesson

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.application.expertnewdesign.Video
import com.application.expertnewdesign.lesson.article.VideoFragment

class SampleFragmentPagerAdapter(private val listVideo: List<Video>, private val fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var fragmentsList: MutableList<Fragment>? = null

    override fun getCount(): Int {
        return listVideo.size
    }

    override fun getItem(position: Int): Fragment {
        return VideoFragment(listVideo[position].code)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listVideo[position].name
    }

    fun removeFragments(){
        if(fragmentsList != null){
            fragmentsList!!.forEach {
                fm.beginTransaction().remove(it).commit()
            }
        }
    }
}