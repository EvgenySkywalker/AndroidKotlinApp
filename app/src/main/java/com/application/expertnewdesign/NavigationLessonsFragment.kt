package com.application.expertnewdesign

import android.os.Bundle
import android.view.*
import android.view.View.*
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.lesson.ArticleFragment
import com.application.expertnewdesign.statistic.CustomAdapter
import com.application.expertnewdesign.statistic.MetadataNavigation
import com.application.expertnewdesign.statistic.Subject
import com.application.expertnewdesign.statistic.Topic
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_lessons_fragment.*
import java.lang.StringBuilder

class NavigationLessonsFragment(metadata: MetadataNavigation): Fragment(){
    val subjectList: List<Subject> = metadata.subjectList
    var currentSubject: Subject? = null
    var currentTopic: Topic? = null
    var lessonPath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.navigation_lessons_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        //toolbar.inflateMenu(R.menu.navigation_lessons)
        val activity = activity!! as MainActivity
        activity.navigationLessonsFragment = this
        showSubjects()
        navigationBack.setOnClickListener {
            activity.onBackPressed()
        }
    }

    fun openLesson(){
        fragmentManager!!.beginTransaction().run{
            add(R.id.fragment_container, LessonLoadingFragment(lessonPath!!), "lesson_loading")
            hide(fragmentManager!!.findFragmentByTag("navigation")!!)
            addToBackStack("loading")
            commit()
        }
        //Без сервера
        /*fragmentManager!!.beginTransaction().run {
            add(R.id.fragment_container, ArticleFragment(""), "article")
            hide(fragmentManager!!.findFragmentByTag("navigation")!!)
            addToBackStack("lesson_stack")
            commit()
        }
        fragmentManager!!.beginTransaction().run{
            remove(fragmentManager!!.findFragmentByTag("lesson_loading")!!)
            commit()
        }
        activity!!.nav_view.visibility = GONE*/
    }

    fun showSubjects(){
        navigationBack.visibility = GONE
        val customAdapter = CustomAdapter(context!!, subjectList)
        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                currentSubject = subjectList[position]
                showTopics()
            }
        })
        recyclerView.adapter = customAdapter
    }

    fun showTopics(){
        val customAdapter = CustomAdapter(context!!, currentSubject!!.topicList)
        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                currentTopic = currentSubject!!.topicList[position]
                showLessons()
            }
        })
        recyclerView.adapter = customAdapter
        navigationBack.visibility = VISIBLE
    }

    fun showLessons(){
        val customAdapter = CustomAdapter(context!!, currentTopic!!.lessonList)
        customAdapter.setOnItemClickListener(object : CustomAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                lessonPath = StringBuilder("/").append(currentSubject!!.name.replace(' ', '+'))
                    .append("/").append(currentTopic!!.name.replace(' ', '+'))
                    .append("/").append(currentTopic!!.lessonList[position].name.replace(' ', '+'))
                    .toString()
                openLesson()
            }
        })
        recyclerView.adapter = customAdapter
    }
}