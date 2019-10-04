package com.application.expertnewdesign.lesson.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import kotlinx.android.synthetic.main.video_fragment.*



class VideoFragment (val code: String): Fragment(){

    interface Layout{
        fun height(_height: Int)
        fun fullScreen(isFullScreen: Boolean)
    }

    var initializedYouTubePlayer: YouTubePlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(video)
        video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(code, 0f)
                initializedYouTubePlayer = youTubePlayer
                val fragment = activity!!.supportFragmentManager.findFragmentByTag("article") as ArticleFragment
                if(!fragment.done){
                    fragment.height(video.measuredHeight)
                    fragment.done = true
                }
            }
        })
        video.addFullScreenListener(object : YouTubePlayerFullScreenListener{
            override fun onYouTubePlayerEnterFullScreen() {
                val fragment = activity!!.supportFragmentManager.findFragmentByTag("article") as ArticleFragment
                fragment.fullScreen(true)
                video.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen(){
                val fragment = activity!!.supportFragmentManager.findFragmentByTag("article") as ArticleFragment
                fragment.fullScreen(false)
                video.exitFullScreen()
            }
        })
    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (!visible && initializedYouTubePlayer != null)
            initializedYouTubePlayer!!.pause()
    }
}