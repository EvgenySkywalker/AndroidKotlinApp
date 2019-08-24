package com.application.expertnewdesign.lesson

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.JsonHelper
import com.application.expertnewdesign.R
import com.application.expertnewdesign.Video
import com.application.expertnewdesign.lesson.article.VideoFragment
import kotlinx.android.synthetic.main.article_fragment.*
import java.io.File
import java.lang.StringBuilder

class ArticleFragment(val path: String): Fragment(), VideoFragment.SetHeight{

    override fun height(_height: Int) {
        if(!done) {
            viewPager.layoutParams.height = _height
            val params = pdfView.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(RelativeLayout.BELOW)
            params.addRule(RelativeLayout.BELOW, R.id.progressBar)
            pdfView.layoutParams = params
        }
    }

    private var playlist: List<Video>? = null

    var done: Boolean = false
    var lastPage: Int = 0
    private var playlistSize: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.article_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        articleToolbar.inflateMenu(R.menu.article)
        articleToolbar.setOnMenuItemClickListener {
            when(it!!.itemId){
                R.id.toTest->{
                    val articleFragment = activity!!.supportFragmentManager.findFragmentByTag("article")
                    activity!!.supportFragmentManager.beginTransaction().run{
                        add(R.id.fragment_container, TestFragment(), "test")
                        hide(articleFragment!!).addToBackStack("lesson_stack")
                        commit()
                    }
                }
                R.id.hideVideo->{
                    val viewPagerAdapter = viewPager.adapter as SampleFragmentPagerAdapter
                    viewPagerAdapter.removeFragments()
                    viewPager.visibility = GONE
                    it.isVisible = false
                    articleToolbar.menu.findItem(R.id.showVideo).isVisible = true
                    progressBar.visibility = GONE
                }
                R.id.showVideo->{
                    viewPager.visibility = VISIBLE
                    getPagerAdapter()
                    it.isVisible = false
                    articleToolbar.menu.findItem(R.id.hideVideo).isVisible = true
                    progressBar.visibility = VISIBLE
                }
                else->{
                    super.onOptionsItemSelected(it)
                }
            }
            true
        }
        val dir = StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("article1.pdf").toString()
        pdfView.fromFile(File(dir)).load()
        /*playlist = JsonHelper(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("videos.json").toString()).listVideo
        playlistSize = playlist!!.size
        progressBar.max = playlistSize!!
        getPagerAdapter()
        tabs.setupWithViewPager(viewPager)*/
        listeners()
    }

    private fun listeners(){
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageSelected(position: Int) {
                progressBar.progress += (position-lastPage)
                lastPage = position
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        articleBack.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    private fun getPagerAdapter(){
        viewPager.adapter = SampleFragmentPagerAdapter(playlist!!, childFragmentManager)
        progressBar.progress = 1
    }
}