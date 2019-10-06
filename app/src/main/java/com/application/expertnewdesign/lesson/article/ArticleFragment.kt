package com.application.expertnewdesign.lesson.article

import android.content.Context.AUDIO_SERVICE
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.JsonHelper
import com.application.expertnewdesign.R
import com.application.expertnewdesign.lesson.test.TestFragment
import com.application.expertnewdesign.lesson.test.question.QuestionMetadata
import com.application.expertnewdesign.navigation.Lesson
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



class ArticleFragment(val path: String): Fragment(), VideoFragment.PlayerLayout{

    private var playlist: List<String>? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var timeInLesson: Long = 0

    var hasHeight: Boolean = false
    var lastPage: Int = 0
    var height: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.article_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        setPlaylist()
        setPdf()
        showTest()
    }

    private fun setToolbar(){
        articleToolbar.inflateMenu(R.menu.article)
        articleBack.setOnClickListener {
            activity!!.onBackPressed()
        }
        articleToolbar.setOnMenuItemClickListener {
            when(it!!.itemId){
                R.id.playAudio->{
                    releaseMP()
                    audioManager = activity!!.getSystemService(AUDIO_SERVICE) as AudioManager
                    mediaPlayer = MediaPlayer()
                    mediaPlayer!!.setDataSource("${activity!!.getExternalFilesDir(null)}/audio.mp3")
                    mediaPlayer!!.prepare()
                    mediaPlayer!!.start()
                }
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
                    playlistProgressBar.visibility = GONE
                }
                R.id.showVideo->{
                    viewPager.visibility = VISIBLE
                    if(!hasHeight){
                        viewPager.layoutParams.height = 0
                    }
                    it.isVisible = false
                    articleToolbar.menu.findItem(R.id.hideVideo).isVisible = true
                    if(playlistProgressBar.max > 1){
                        playlistProgressBar.visibility = VISIBLE
                    }
                }
                else->{
                    super.onOptionsItemSelected(it)
                }
            }
            true
        }
    }

    private fun setPlaylist(){
        playlist = JsonHelper(StringBuilder(context!!.getExternalFilesDir(null).toString())
            .append(path).append("videos.json")
            .toString())
            .listVideo
        if(playlist != null) {
            if (playlist!!.isNotEmpty()) {
                articleToolbar.menu.findItem(R.id.showVideo).isVisible = true
                playlistProgressBar.max = playlist!!.size
                if (playlistProgressBar.max > 1) {
                    playlistProgressBar.visibility = VISIBLE
                }
                viewPager.adapter = VideoFragmentPagerAdapter(playlist!!, childFragmentManager)
                playlistProgressBar.progress = 1
                tabs.setupWithViewPager(viewPager)
                viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageSelected(position: Int) {
                        playlistProgressBar.progress += (position - lastPage)
                        lastPage = position
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float,
                                                positionOffsetPixels: Int
                    ) {}

                    override fun onPageScrollStateChanged(state: Int) {}
                })
            }
        }
    }

    private fun setPdf(){
        val pdf = File("${context!!.getExternalFilesDir(null)}${path}article.pdf")
        if(pdf.exists()){
            pdfView.fromFile(pdf)
                .spacing(0)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load()
        }else{
            pdfView.visibility = GONE
        }
    }

    private fun showTest(){
        val file = File(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("questions.json").toString())
        if(file.exists()) {
            val meta = Json(JsonConfiguration.Stable).parse(
                QuestionMetadata.serializer().list,
                file.readText()
            )
            if (meta.isNotEmpty()) {
                articleToolbar.menu.findItem(R.id.toTest).isVisible = true
            }
        }
    }

    override fun height(_height: Int) {
        if(!hasHeight) {
            height = _height
            viewPager.layoutParams.height = height!!
        }
    }

    override fun fullScreen(isFullScreen: Boolean) {
        if(isFullScreen){
            articleToolbar.visibility = GONE
            pdfView.visibility = GONE
            val params = viewPager.layoutParams as LinearLayout.LayoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity!!.window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_IMMERSIVE
                    .or(SYSTEM_UI_FLAG_FULLSCREEN
                        .or(SYSTEM_UI_FLAG_HIDE_NAVIGATION))
        }else{
            articleToolbar.visibility = VISIBLE
            pdfView.visibility = VISIBLE
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity!!.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE
            val params = viewPager.layoutParams as RelativeLayout.LayoutParams
            params.height = height!!
            viewPager.layoutParams = params
        }
    }

    private fun publishStat(stat: Statistic){
        val profileFragment = activity!!.supportFragmentManager.findFragmentByTag("profile") as ProfileFragment
        profileFragment.addStat(stat)
    }

    override fun onResume() {
        super.onResume()

        timeInLesson = Calendar.getInstance().timeInMillis
    }

    override fun onPause() {
        super.onPause()

        if(timeInLesson.compareTo(0) != 0) {
            val currentTime = Calendar.getInstance().timeInMillis
            val lesson = Lesson(path)

            lesson.time = currentTime - timeInLesson
            timeInLesson = 0
            Thread().run {
                publishStat(lesson)
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

    private fun releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer!!.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        releaseMP()
    }
}