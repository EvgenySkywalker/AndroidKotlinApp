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
import com.application.expertnewdesign.lesson.article.ArticleFragment
import com.application.expertnewdesign.navigation.NavigationLessonsFragment
import com.application.expertnewdesign.profile.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*
import android.view.inputmethod.InputMethodManager
import com.application.expertnewdesign.chat.ChatFragment
import com.application.expertnewdesign.lesson.test.TestFragment
import com.application.expertnewdesign.profile.TimeObject
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.navigation_lessons_fragment.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query


interface LessonStatAPI{
    @PUT("updateSelfTimeSpent")
    fun putLessonInfo(@Header("Authorization") token: String,
                    @Query("course") subject: String,
                    @Query("subject") topic: String,
                    @Query("lesson") lesson: String,
                    @Query("timeSpent") timeSpent: String): Call<ResponseBody>
}

class MainActivity : AppCompatActivity() {

    val LESSON_CHANNEL_ID = "loading_lesson"
    var navigationLessonsFragment: NavigationLessonsFragment? = null
    var chatFragment: ChatFragment? = null
    var profileFragment: ProfileFragment? = null

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(container.windowToken, 0)
        val metadataFragment = supportFragmentManager.findFragmentByTag("metadata_loading")
        val lessonLoadingFragment = supportFragmentManager.findFragmentByTag("lesson_loading")
        when (item.itemId) {
            R.id.navigation_lessons -> {
                supportFragmentManager.beginTransaction().run {
                    if(chatFragment != null){
                        hide(chatFragment!!)
                    }
                    if(profileFragment != null){
                        hide(profileFragment!!)
                    }
                    if(navigationLessonsFragment != null) {
                        show(navigationLessonsFragment!!)
                    }
                    if(metadataFragment != null){
                        show(metadataFragment)
                    }
                    if(lessonLoadingFragment != null){
                        show(lessonLoadingFragment)
                    }
                    commit()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                if(profileFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        if(navigationLessonsFragment != null) {
                            hide(navigationLessonsFragment!!)
                        }
                        if(chatFragment != null) {
                            hide(chatFragment!!)
                        }
                        if(metadataFragment != null){
                            hide(metadataFragment)
                        }
                        if(lessonLoadingFragment != null){
                            hide(lessonLoadingFragment)
                        }
                        show(profileFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }
            R.id.navigation_chat -> {
                if(chatFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        if(navigationLessonsFragment != null) {
                            hide(navigationLessonsFragment!!)
                        }
                        if(profileFragment != null) {
                            hide(profileFragment!!)
                        }
                        if(metadataFragment != null){
                            hide(metadataFragment)
                        }
                        if(lessonLoadingFragment != null){
                            hide(lessonLoadingFragment)
                        }
                        show(chatFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }
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

    override fun onStart() {
        super.onStart()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(container.windowToken, 0)

        supportFragmentManager.beginTransaction().run{
            when(nav_view.selectedItemId){
                0->{
                    hide(chatFragment!!)
                    hide(profileFragment!!)
                }
                1->{
                    if(navigationLessonsFragment != null) {
                        hide(navigationLessonsFragment!!)
                    }
                    hide(chatFragment!!)
                }
                2->{
                    if(navigationLessonsFragment != null) {
                        hide(navigationLessonsFragment!!)
                    }
                    hide(profileFragment!!)
                }
            }
            commit()
        }
        nav_view.isSelected = false
    }

    private fun init(){
        createNotificationChannel()
        supportFragmentManager.beginTransaction().run{
            add(R.id.fragment_container, ProfileFragment(), "profile")
            add(R.id.fragment_container, ChatFragment(), "chat")
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
        val article = supportFragmentManager.findFragmentByTag("article")
        if(article != null){
            if((article as ArticleFragment).isFullScreen){
                article.exitFullScreenMode()
                return
            }
        }
        val testFragment = supportFragmentManager.findFragmentByTag("test")
        if(testFragment is TestFragment) {
            if (testFragment.isVisible) {
                if (testFragment.isFinal) {
                    testFragment.showExitAccept()
                } else {
                    super.onBackPressed()
                }
                return
            }
        }
        if (nav_view.visibility == GONE) {
            nav_view.visibility = VISIBLE
        }
        nav_view.isSelected = false
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(container.windowToken, 0)
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()

        Thread().run{
            val user = profileFragment!!.user
            user.lessonsStat.forEach {
                putLessonStat(it, profileFragment!!, it == user.lessonsStat[0])
            }
        }
    }

    private fun putLessonStat(stat: TimeObject, profileFragment: ProfileFragment, isFirst: Boolean){

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val lessonAPI = retrofit.create(LessonStatAPI::class.java)
        val (_,subject, topic, lesson) = stat.lesson.split("/")
        val token = intent.getStringExtra("token")!!
        lessonAPI.putLessonInfo("Token $token", subject, topic, lesson, stat.time.toString()).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(!response.isSuccessful){
                    if(isFirst) {
                        val json = JsonHelper(filesDir.path)
                        json.toJson(profileFragment.user)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if(isFirst) {
                    val json = JsonHelper(filesDir.path)
                    val user = profileFragment.user
                    if(user.name != null) {
                        json.toJson(profileFragment.user)
                    }
                }
            }
        })
    }

}
