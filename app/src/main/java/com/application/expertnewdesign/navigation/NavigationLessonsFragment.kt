package com.application.expertnewdesign.navigation

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.application.expertnewdesign.LessonLoadingFragment
import com.application.expertnewdesign.MainActivity
import com.application.expertnewdesign.R
import kotlinx.android.synthetic.main.navigation_lessons_fragment.*
import kotlinx.android.synthetic.main.recycler_view_item.view.*
import java.lang.StringBuilder
import java.util.*

class NavigationLessonsFragment(metadata: MetadataNavigation): Fragment(){
    val subjectList: List<Subject> = metadata.subjectList
    var currentSubject: Subject? = null
    var currentTopic: Topic? = null
    var currentLesson: Lesson? = null
    var lessonPath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.navigation_lessons_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            add(R.id.fragment_container,
                LessonLoadingFragment(lessonPath!!), "lesson_loading")
            hide(fragmentManager!!.findFragmentByTag("navigation")!!)
            hide(fragmentManager!!.findFragmentByTag("profile")!!)
            hide(fragmentManager!!.findFragmentByTag("chat")!!)
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
        val customAdapter = CustomAdapter(
            context!!,
            subjectList.sortedBy { it.name })
        customAdapter.setOnItemClickListener(object :
            CustomAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                currentSubject = subjectList.sortedBy { it.name }[position]
                showTopics()
            }
        })
        recyclerView.adapter = customAdapter
    }

    fun showTopics(){
        val customAdapter = CustomAdapter(
            context!!,
            currentSubject!!.topicList.sortedBy { it.name })
        customAdapter.setOnItemClickListener(object :
            CustomAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                currentTopic = currentSubject!!.topicList.sortedBy { it.name }[position]
                showLessons()
            }
        })
        recyclerView.adapter = customAdapter
        navigationBack.visibility = VISIBLE
    }

    fun showLessons(){
        val customAdapter = CustomAdapter(
            context!!,
            currentTopic!!.lessonList.sortedBy { it.name })
        customAdapter.setOnItemClickListener(object :
            CustomAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                currentLesson = currentTopic!!.lessonList.sortedBy { it.name }[position]
                lessonPath = StringBuilder("/")
                    .append(currentSubject!!.name)
                    .append("/").append(currentTopic!!.name)
                    .append("/").append(currentLesson!!.name)
                    .toString()
                openLesson()
            }
        })
        recyclerView.adapter = customAdapter
    }
}

class CustomAdapter(context: Context, private val items: List<Statistic>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(onEntryClickListener: OnItemClickListener) {
        mOnItemClickListener = onEntryClickListener
    }

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if(item is Subject){
            holder.title.text = item.name
            holder.subtitle.text = StringBuilder().append("Количество тем: ").append(item.topicList.size).toString()
        }
        if(item is Topic){
            holder.title.text = item.name
            holder.subtitle.text = StringBuilder().append("Количество тем: ").append(item.lessonList.size).toString()
        }
        if(item is Lesson){
            holder.title.text = item.name
            holder.subtitle.text = StringBuilder().append(item.description).toString()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        //val image = view.image
        val title = view.title
        val subtitle = view.subtitle

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(v, layoutPosition)
            }
        }
    }
}