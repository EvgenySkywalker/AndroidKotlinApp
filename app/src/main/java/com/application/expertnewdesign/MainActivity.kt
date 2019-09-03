package com.application.expertnewdesign

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.application.expertnewdesign.lesson.ArticleFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_lessons_fragment.*
import kotlinx.android.synthetic.main.video_fragment.*


class MainActivity : AppCompatActivity() {

    val LESSON_CHANNEL_ID = "loading_lesson"

    var navigationLessonsFragment: NavigationLessonsFragment? = null
    //var historyFragment: NavigationLessonsFragment? = null
    var chatFragment: NavigationLessonsFragment? = null
    //var profileFragment: NavigationLessonsFragment? = null

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_lessons -> {
                if(navigationLessonsFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        show(navigationLessonsFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }
            /*R.id.navigation_history -> {
                if(historyFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        hide(navigationLessonsFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }*/
            R.id.navigation_chat -> {
                if(chatFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        hide(navigationLessonsFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }
            /*R.id.navigation_profile -> {
                if(profileFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        hide(navigationLessonsFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }*/
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        init()
    }

    private fun init(){
        createNotificationChannel()
        supportFragmentManager.beginTransaction().run{
            add(R.id.fragment_container, MetadataLoadingFragment(), "metadata_loading")
            commit()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Загрузка"
            val descriptionText = "Скачивание урока"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(LESSON_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBackPressed() {
        if(navigationLessonsFragment!!.isVisible){
            if(navigationLessonsFragment!!.currentTopic != null){
                navigationLessonsFragment!!.currentTopic = null
                navigationLessonsFragment!!.showTopics()
                return
            }else if(navigationLessonsFragment!!.currentSubject != null){
                navigationLessonsFragment!!.currentSubject = null
                navigationLessonsFragment!!.showSubjects()
                navigationBack.visibility = GONE
                return
            }
        }
        /*if(video != null){
            if(video.isFullScreen()){
                val fragment = supportFragmentManager.findFragmentByTag("article") as ArticleFragment
                fragment.fullScreen(false)
                video.exitFullScreen()
            }
        }*/
        val testFragment = supportFragmentManager.findFragmentByTag("test")
        if(testFragment != null){
            if(testFragment.isVisible){
                super.onBackPressed()
                return
            }
        }
        if (nav_view.visibility == GONE) {
            nav_view.visibility = VISIBLE
        }
        super.onBackPressed()
    }
}
