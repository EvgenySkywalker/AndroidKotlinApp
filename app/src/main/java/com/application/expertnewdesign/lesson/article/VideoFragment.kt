package com.application.expertnewdesign.lesson.article

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import kotlinx.android.synthetic.main.video_fragment.*

class VideoFragment (var pager: ViewPager, val code: String): Fragment(){

    var initializedYouTubePlayer: YouTubePlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setYouTubePlayer()
    }

    private fun setYouTubePlayer(){
        lifecycle.addObserver(video)
        video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(code, 0f)
                initializedYouTubePlayer = youTubePlayer

                if((pager.adapter as VideoFragmentPagerAdapter).portrait == 0) {
                    val height = video.measuredHeight
                    (pager.adapter as VideoFragmentPagerAdapter).portrait = height
                    pager.layoutParams.height = height
                    val article = (activity!!.supportFragmentManager.findFragmentByTag("article") as ArticleFragment)
                    article.showVideoButton()
                    if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                        if(article.isFullScreen) {
                            article.enterFullScreenMode()
                        }
                    }
                    if(!article.isFullScreen) {
                        pager.visibility = GONE
                    }
                }
            }
        })
        video.addFullScreenListener(object : YouTubePlayerFullScreenListener{
            override fun onYouTubePlayerEnterFullScreen() {
                val article = (activity!!.supportFragmentManager.findFragmentByTag("article") as ArticleFragment)
                if(article.isFullScreen){
                    article.exitFullScreenMode()
                }else{
                    article.enterFullScreenMode()
                }

            }

            override fun onYouTubePlayerExitFullScreen(){
                val article = (activity!!.supportFragmentManager.findFragmentByTag("article") as ArticleFragment)
                if(article.isFullScreen){
                    article.exitFullScreenMode()
                }else{
                    article.enterFullScreenMode()
                }
            }
        })
    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (!visible && initializedYouTubePlayer != null)
            initializedYouTubePlayer!!.pause()
    }
}