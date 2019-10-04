package com.application.expertnewdesign.lesson.article

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.JsonHelper
import com.application.expertnewdesign.R
import com.application.expertnewdesign.lesson.test.TestFragment
import com.application.expertnewdesign.lesson.test.question.QuestionMetadata
import com.application.expertnewdesign.navigation.Lesson
import com.application.expertnewdesign.navigation.NavigationLessonsFragment
import com.application.expertnewdesign.navigation.Statistic
import com.application.expertnewdesign.profile.ProfileFragment
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.android.synthetic.main.article_fragment.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File
import java.lang.StringBuilder
import java.util.*

class ArticleFragment(val path: String): Fragment(), VideoFragment.Layout{

    private var playlist: List<String>? = null
    lateinit var lesson: Lesson

    var done: Boolean = false
    var lastPage: Int = 0
    var height: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.article_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        articleToolbar.inflateMenu(R.menu.article)
        articleToolbar.setOnMenuItemClickListener {
            when(it!!.itemId){
                R.id.toTest->{
                    if(viewPager.adapter != null) {
                        val adapter = viewPager.adapter as VideoFragmentPagerAdapter
                        if(adapter.fragmentsList.isNotEmpty()) {
                            if(adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer != null) {
                                adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer!!.pause()
                            }
                        }
                    }
                    val articleFragment =
                        activity!!.supportFragmentManager.findFragmentByTag("article")
                    activity!!.supportFragmentManager.beginTransaction().run {
                        add(R.id.fragment_container, TestFragment(path), "test")
                        hide(articleFragment!!).addToBackStack("lesson_stack")
                        commit()
                    }
                }
                R.id.hideVideo->{
                    if(viewPager.adapter != null) {
                        val adapter = viewPager.adapter as VideoFragmentPagerAdapter
                        if (adapter.fragmentsList.isNotEmpty()) {
                            if (adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer != null) {
                                adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer!!.pause()
                            }
                        }
                    }
                    viewPager.visibility = GONE
                    it.isVisible = false
                    articleToolbar.menu.findItem(R.id.showVideo).isVisible = true
                    progressBar.visibility = GONE
                }
                R.id.showVideo->{
                    viewPager.visibility = VISIBLE
                    it.isVisible = false
                    articleToolbar.menu.findItem(R.id.hideVideo).isVisible = true
                    if(progressBar.max > 1){
                        progressBar.visibility = VISIBLE
                    }
                }
                else->{
                    super.onOptionsItemSelected(it)
                }
            }
            true
        }
        val dir = StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("article.pdf").toString()
        pdfView.fromFile(File(dir)).spacing(0).pageFitPolicy(FitPolicy.WIDTH).load()
        playlist = JsonHelper(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("videos.json").toString()).listVideo
        if(playlist != null) {
            if (playlist!!.isNotEmpty()) {
                setPlaylist()
            } else {
                articleToolbar.menu.findItem(R.id.showVideo).isVisible = false
            }
        }else{
            articleToolbar.menu.findItem(R.id.showVideo).isVisible = false
        }
        val file = File(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("questions.json").toString())
        if(file.exists()) {
            val meta = Json(JsonConfiguration.Stable).parse(
                QuestionMetadata.serializer().list,
                file.readText()
            )
            if (meta.isEmpty()) {
                articleToolbar.menu.findItem(R.id.toTest).isVisible = false
            }
        }else{
            articleToolbar.menu.findItem(R.id.toTest).isVisible = false
        }
        setLesson()
        articleBack.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    private fun setPlaylist(){
            progressBar.max = playlist!!.size
            if (progressBar.max <= 1) {
                progressBar.visibility = GONE
            }
            getPagerAdapter()
            tabs.setupWithViewPager(viewPager)
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    progressBar.progress += (position - lastPage)
                    lastPage = position
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
    }

    private fun getPagerAdapter(){
        viewPager.adapter = VideoFragmentPagerAdapter(playlist!!, childFragmentManager)
        progressBar.progress = 1
    }

    override fun height(newHeight: Int) {
        if(!done) {
            height = newHeight
            viewPager.layoutParams.height = height!!
            val params = pdfView.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(RelativeLayout.BELOW)
            params.addRule(RelativeLayout.BELOW, R.id.progressBar)
            pdfView.layoutParams = params
        }
    }

    override fun fullScreen(isFullScreen: Boolean) {
        if(isFullScreen){
            val params = viewPager.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(RelativeLayout.BELOW)
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity!!.window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_IMMERSIVE
                    .or(SYSTEM_UI_FLAG_FULLSCREEN
                        .or(SYSTEM_UI_FLAG_HIDE_NAVIGATION))
        }else{
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity!!.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE
            val params = viewPager.layoutParams as RelativeLayout.LayoutParams
            params.height = height!!
            params.addRule(RelativeLayout.BELOW, R.id.articleToolbar)
            viewPager.layoutParams = params
        }
    }

    private fun publishStat(stat: Statistic){
        val profileFragment = activity!!.supportFragmentManager.findFragmentByTag("profile") as ProfileFragment
        profileFragment.addStat(stat)
    }

    override fun onResume() {
        super.onResume()

        val currentTime = Calendar.getInstance().timeInMillis
        lesson.time = currentTime
    }

    override fun onPause() {
        super.onPause()

        if(lesson.time.compareTo(0) != 0) {
            val currentTime = Calendar.getInstance().timeInMillis
            val timeInLesson = Lesson(path)

            timeInLesson.time = currentTime - lesson.time
            lesson.time = 0
            Thread().run {
                publishStat(timeInLesson)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if(!isHidden){
            onResume()
        }else{
            onPause()
        }
    }

    //Для статистики
    private fun setLesson(){
        val navigationFragment =
            fragmentManager!!.findFragmentByTag("navigation") as NavigationLessonsFragment
        lesson = Lesson(navigationFragment.currentLesson!!.name, null)
    }
}