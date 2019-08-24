package com.application.expertnewdesign

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_lessons_fragment.*

class MainActivity : AppCompatActivity() {

    var navigationLessonsFragment: NavigationLessonsFragment? = null
    var historyFragment: NavigationLessonsFragment? = null
    var chatFragment: NavigationLessonsFragment? = null
    var profileFragment: NavigationLessonsFragment? = null

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
            R.id.navigation_history -> {
                if(historyFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        hide(navigationLessonsFragment!!)
                        commit()
                    }
                    return@OnNavigationItemSelectedListener true
                }
                return@OnNavigationItemSelectedListener false
            }
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
            R.id.navigation_profile -> {
                if(profileFragment != null) {
                    supportFragmentManager.beginTransaction().run {
                        hide(navigationLessonsFragment!!)
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

    private fun init(){
        supportFragmentManager.beginTransaction().run{
            add(R.id.fragment_container, MetadataLoadingFragment(), "metadata_loading")
            commit()
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
