package com.application.expertnewdesign.lesson.article

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.View.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.*
import com.application.expertnewdesign.lesson.test.TestFragment
import com.application.expertnewdesign.lesson.test.TestFragmentPagerAdapter
import com.application.expertnewdesign.lesson.test.question.QuestionMetadata
import com.application.expertnewdesign.navigation.Lesson
import com.application.expertnewdesign.navigation.Statistic
import com.application.expertnewdesign.profile.ProfileFragment
import com.application.expertnewdesign.profile.TestObject
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.article_fragment.*
import kotlinx.android.synthetic.main.article_fragment.tabs
import kotlinx.android.synthetic.main.article_fragment.viewPager
import kotlinx.android.synthetic.main.loading_fragment.*
import kotlinx.android.synthetic.main.music_player.*
import kotlinx.android.synthetic.main.test_fragment.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.io.File
import java.lang.StringBuilder
import java.util.*

interface ExamAPI{
    @GET("getExamTime")
    fun getExam(@Header("Authorization") token: String,
                @Query("course") subject: String,
                @Query("subject") topic: String,
                @Query("lesson") lesson: String): Call<ResponseBody>
}

class ArticleFragment(val path: String, val time: Long = -1): Fragment() {

    lateinit var intent: Intent
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null

    private var duration: Int = 0
    private var lastSeekTime: Long = 0
    val timerHandler = Handler()
    private val timerRunnable = object : Runnable {

        override fun run() {

            val millis = mediaPlayer!!.currentPosition
            val allSeconds = millis / 1000
            val minutes = allSeconds / 60
            val seconds = allSeconds % 60
            val durationSeconds = duration / 1000

            if (currentPosition != null) {
                if (duration - millis > 200) {
                    progress.progress = ((millis.toFloat() / duration.toFloat()) * 100).toInt()
                    currentPosition.text = String.format(
                        "%d:%02d / %d:%02d",
                        minutes,
                        seconds,
                        durationSeconds / 60,
                        durationSeconds % 60
                    )
                } else {
                    musicPause.visibility = GONE
                    musicPlay.visibility = VISIBLE
                    currentPosition.text = String.format(
                        "%d:%02d / %d:%02d",
                        durationSeconds / 60,
                        durationSeconds % 60,
                        durationSeconds / 60,
                        durationSeconds % 60
                    )
                    progress.progress = 100
                }
            }

            timerHandler.postDelayed(this, 200)
        }
    }

    var isFullScreen = false
    val getHeight = Handler()
    private val heightRunnable = object : Runnable {

        override fun run() {
            if(viewPager != null) {
                if ((viewPager.adapter as VideoFragmentPagerAdapter).portrait != 0) {
                    activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    getHeight.postDelayed(this, 200)
                }
            }
        }
    }
    private var playlist: List<String>? = null
    var lastPage: Int = 0

    private var timeInLesson: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.article_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(time.compareTo(-1) != 0){
            val file = File("${activity!!.getExternalFilesDir(null)}${path}config.cfg")
            file.bufferedWriter().use{
                it.write(time.toString())
            }
        }
        setToolbar()
        setMusicPlayer()
        setPlaylist()
        setPdf()
        showTest()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (!isHidden) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                articleToolbar.menu.findItem(R.id.fullScreen).isVisible = true
                enterFullScreenMode()
            }

            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                articleToolbar.menu.findItem(R.id.fullScreen).isVisible = false
                if (isFullScreen) {
                    exitFullScreenMode()
                }
            }
        }
    }

    private fun setToolbar() {
        articleToolbar.inflateMenu(R.menu.article)
        articleBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        articleToolbar.setOnMenuItemClickListener {
            when (it!!.itemId) {
                R.id.playAudio -> {
                    hideVideo()
                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer()
                        mediaPlayer!!.setDataSource("${activity!!.getExternalFilesDir(null)}${path}podcast.mp3")
                        mediaPlayer!!.prepare()
                        mediaPlayer!!.start()
                        musicPlayer.visibility = VISIBLE
                        duration = mediaPlayer!!.duration
                        timerHandler.post(timerRunnable)
                    } else {
                        if (musicPlayer.visibility == GONE) {
                            musicPlayer.visibility = VISIBLE
                        } else {
                            musicPlayer.visibility = GONE
                        }
                    }
                }
                R.id.toTest -> {
                    if (viewPager.adapter != null) {
                        val adapter = viewPager.adapter as VideoFragmentPagerAdapter
                        if (adapter.fragmentsList.isNotEmpty()) {
                            if (adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer != null) {
                                adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer!!.pause()
                            }
                        }
                    }
                    showTestPicker()
                }
                R.id.hideVideo -> {
                    hideVideo()
                }
                R.id.showVideo -> {
                    if(mediaPlayer != null) {
                        if (mediaPlayer!!.isPlaying) {
                            mediaPlayer!!.pause()
                            musicPause.visibility = GONE
                            musicPlay.visibility = VISIBLE
                        }
                        musicPlayer.visibility = GONE
                    }
                    viewPager.visibility = VISIBLE
                    it.isVisible = false
                    articleToolbar.menu.findItem(R.id.hideVideo).isVisible = true
                    if (playlistProgressBar.max > 1) {
                        playlistProgressBar.visibility = VISIBLE
                    }
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        enterFullScreenMode()
                    }
                }
                R.id.fullScreen -> {
                    enterFullScreenMode()
                }
                else -> {
                    super.onOptionsItemSelected(it)
                }
            }
            true
        }
    }

    private fun setMusicPlayer() {
        audioManager = activity!!.getSystemService(AUDIO_SERVICE) as AudioManager
        progress.progressDrawable.colorFilter =
            PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)

        if (File("${activity!!.getExternalFilesDir(null)}${path}podcast.mp3").exists()) {
            articleToolbar.menu.findItem(R.id.playAudio).isVisible = true
        }

        musicPause.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
            musicPause.visibility = GONE
            musicPlay.visibility = VISIBLE
        }

        musicPlay.setOnClickListener {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
            musicPause.visibility = VISIBLE
            musicPlay.visibility = GONE
        }

        musicBackward.setOnClickListener {
            val currentTime = Calendar.getInstance().timeInMillis
            if (currentTime - lastSeekTime > 500) {
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition - 5000)
            } else {
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition - 12000)
            }
            lastSeekTime = currentTime
        }

        musicForward.setOnClickListener {
            val currentTime = Calendar.getInstance().timeInMillis
            if (currentTime - lastSeekTime > 500) {
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition + 5000)
            } else {
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition + 12000)
            }
            lastSeekTime = currentTime
        }
    }

    private fun setPlaylist() {
        playlist = JsonHelper(
            StringBuilder(context!!.getExternalFilesDir(null).toString())
                .append(path).append("videos.json")
                .toString()
        )
            .listVideo
        viewPager.layoutParams.height = 0
        if (playlist != null) {
            if (playlist!!.isNotEmpty()) {
                activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                playlistProgressBar.max = playlist!!.size
                playlistProgressBar.progressDrawable.colorFilter =
                    PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                viewPager.adapter =
                    VideoFragmentPagerAdapter(viewPager, playlist!!, childFragmentManager)
                getHeight.post(heightRunnable)
                playlistProgressBar.progress = 1
                tabs.setupWithViewPager(viewPager)
                viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageSelected(position: Int) {
                        playlistProgressBar.progress += (position - lastPage)
                        lastPage = position
                    }

                    override fun onPageScrolled(
                        position: Int, positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageScrollStateChanged(state: Int) {}
                })
            }
        }
    }

    private fun setPdf() {
        val pdf = File("${context!!.getExternalFilesDir(null)}${path}article.pdf")
        if (pdf.exists()) {
            if (pdf.length() > 0) {
                pdfView.fromFile(pdf)
                    .spacing(0)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load()
                return
            }
        }
        pdfView.visibility = GONE
        /*if (playlist != null) {
            if (playlist!!.isNotEmpty()) {
                viewPager.visibility = VISIBLE
                articleToolbar.menu.findItem(R.id.hideVideo).isVisible = true
                if (playlistProgressBar.max > 1) {
                    playlistProgressBar.visibility = VISIBLE
                }
            }
        }*/
    }

    private fun showTest() {
        val file =
            File("${context!!.getExternalFilesDir(null)}${path}exercise.json")
        if (file.exists()) {
            val meta = Json(JsonConfiguration.Stable).parse(
                QuestionMetadata.serializer().list,
                file.readText()
            )
            if (meta.isNotEmpty()) {
                articleToolbar.menu.findItem(R.id.toTest).isVisible = true
            }
        }
    }

    fun enterFullScreenMode() {
        articleToolbar.visibility = GONE
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity!!.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            .or(
                SYSTEM_UI_FLAG_FULLSCREEN
                    .or(
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            .or(
                                SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    .or(
                                        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            .or(SYSTEM_UI_FLAG_LAYOUT_STABLE)
                                    )
                            )
                    )
            )
        if (viewPager.visibility == VISIBLE) {
            viewPager.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        isFullScreen = true
    }

    fun exitFullScreenMode() {
        articleToolbar.visibility = VISIBLE
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        activity!!.window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE
        viewPager.layoutParams.height = (viewPager.adapter as VideoFragmentPagerAdapter).portrait
        if (viewPager.visibility == VISIBLE) {
            (viewPager.adapter as VideoFragmentPagerAdapter)
                .fragmentsList[viewPager.currentItem]
                .initializedYouTubePlayer!!.pause()
            viewPager.visibility = GONE
            articleToolbar.menu.findItem(R.id.hideVideo).isVisible = false
            articleToolbar.menu.findItem(R.id.showVideo).isVisible = true
            playlistProgressBar.visibility = GONE
        }
        isFullScreen = false
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, _intent: Intent) {

            if (_intent.action == path) {

                val download = _intent.getParcelableExtra<Download>("download")
                if (download!!.progress == 100) {
                    val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
                    bManager.unregisterReceiver(this)
                }
            }else{
                val download = _intent.getParcelableExtra<Download>("download")
                if (download!!.progress == 100) {

                }
            }
        }
    }

    private fun getPodcast() {
        val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
        val intentFilter = IntentFilter()
        intentFilter.addAction(path)

        intent = Intent(context, DownloadService::class.java)
        intent.putExtra("path", path)
        intent.putExtra("token", activity!!.intent.getStringExtra("token"))

        bManager.registerReceiver(broadcastReceiver, intentFilter)
        activity!!.startService(intent)
    }

    private fun publishStat(stat: Statistic) {
        val profileFragment =
            activity!!.supportFragmentManager.findFragmentByTag("profile") as ProfileFragment
        profileFragment.addStat(stat)
    }

    override fun onResume() {
        super.onResume()

        if (timeInLesson.compareTo(0) == 0) {
            timeInLesson = Calendar.getInstance().timeInMillis
        }
    }

    override fun onPause() {
        super.onPause()

        if (timeInLesson.compareTo(0) != 0) {
            val currentTime = Calendar.getInstance().timeInMillis
            val lesson = Lesson(path)

            lesson.time = currentTime - timeInLesson
            timeInLesson = 0
            Thread().run {
                publishStat(lesson)
            }
        }
    }

    fun showVideoButton() {
        articleToolbar.menu.findItem(R.id.showVideo).isVisible = true
    }

    private fun showTestPicker() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выбор теста:")
            .setMessage("""Выберите тип тестирования""".trimMargin())
            .setPositiveButton("Контрольный") { dialog, _ ->
                dialog.cancel()
                showFinalTestAlert()
            }
            .setNegativeButton("Тренировочный") { dialog, _ ->
                dialog.cancel()
                if(mediaPlayer != null) {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.pause()
                        musicPause.visibility = GONE
                        musicPlay.visibility = VISIBLE
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
        val alert = builder.create()
        alert.show()
        val positiveButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL)
        val parent = positiveButton.parent as LinearLayout
        parent.gravity = Gravity.CENTER_HORIZONTAL
        parent.setPadding(0, 0, 0, 20)
        val leftSpacer = parent.getChildAt(1)
        leftSpacer.visibility = GONE
    }

    private fun showFinalTestAlert(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Внимание")
            .setMessage("""Убедитесь, что ближайшие 20 минут
                |вас ничто не будет отвлекать
                |Контроль можно пройти только один раз
            """.trimMargin())
            .setPositiveButton("Начало") { dialog, _ ->
                dialog.cancel()
                getPass()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
        val positiveButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL)
        val parent = positiveButton.parent as LinearLayout
        parent.gravity = Gravity.CENTER_HORIZONTAL
        parent.setPadding(0, 0, 0, 20)
        val leftSpacer = parent.getChildAt(1)
        leftSpacer.visibility = GONE
    }

    private fun getPass(){
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val examAPI = retrofit.create(ExamAPI::class.java)
        val (_,subject, topic, lesson) = path.split("/")
        val token = activity!!.intent.getStringExtra("token")!!
        examAPI.getExam("Token $token", subject, topic, lesson).enqueue(
            object: Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful){
                        val time = response.body().string().toLong()
                        if(time > 0){
                            if(mediaPlayer != null) {
                                if (mediaPlayer!!.isPlaying) {
                                    mediaPlayer!!.pause()
                                    musicPause.visibility = GONE
                                    musicPlay.visibility = VISIBLE
                                }
                            }
                            val articleFragment =
                                activity!!.supportFragmentManager.findFragmentByTag("article")
                            activity!!.supportFragmentManager.beginTransaction().run {
                                add(R.id.fragment_container, TestFragment(path, isFinal = true, time = time-10*1000), "test")
                                hide(articleFragment!!).addToBackStack("lesson_stack")
                                commit()
                            }
                        }else{
                            Toast.makeText(context!!, "Тест уже пройден", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }
            }
        )
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

    fun hideVideo(){
        if (viewPager.adapter != null) {
            val adapter = viewPager.adapter as VideoFragmentPagerAdapter
            if (adapter.fragmentsList.isNotEmpty()) {
                if (adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer != null) {
                    adapter.fragmentsList[viewPager.currentItem].initializedYouTubePlayer!!.pause()
                }
            }
        }
        viewPager.visibility = GONE
        if(articleToolbar.menu.findItem(R.id.hideVideo).isVisible) {
            articleToolbar.menu.findItem(R.id.hideVideo).isVisible = false
            articleToolbar.menu.findItem(R.id.showVideo).isVisible = true
        }
        playlistProgressBar.visibility = GONE
    }

    override fun onDestroy() {
        timerHandler.removeCallbacks(timerRunnable)
        releaseMP()
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
        bManager.unregisterReceiver(broadcastReceiver)
        activity!!.stopService(intent)
        super.onDestroy()
    }
}