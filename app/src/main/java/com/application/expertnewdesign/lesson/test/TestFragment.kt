package com.application.expertnewdesign.lesson.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.R
import com.application.expertnewdesign.lesson.article.ArticleFragment
import com.application.expertnewdesign.lesson.test.question.*
import com.application.expertnewdesign.navigation.Lesson
import com.application.expertnewdesign.navigation.NavigationLessonsFragment
import com.application.expertnewdesign.navigation.Statistic
import com.application.expertnewdesign.profile.ProfileFragment
import kotlinx.android.synthetic.main.test_fragment.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File
import java.lang.Exception
import java.util.*

class TestFragment(val path: String) : Fragment(){

    //Для статистики
    private lateinit var lesson: Lesson

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        //Для статистики
        setLesson()

        //Тут распарсить вопросики
        setTest()

        //Сюды класть вопросики
        val list: List<Question> = arrayListOf()

        viewPager.adapter = TestFragmentPagerAdapter(childFragmentManager, list, dots_layout, context!!)
        tabs.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                val testAdapter = viewPager.adapter as TestFragmentPagerAdapter
                testAdapter.dotsList[position].setImageResource(R.drawable.ic_dot_selected)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        testBack.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    //С этим делай че хошь. Будет кайф, если будет выплевывать вопросики
    private fun setTest() {
        val file = File(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("questions.json").toString())

        val meta = Json(JsonConfiguration.Stable).parse(QuestionMetadata.serializer().list, file.readText())

        val questions = emptyList<Question>().toMutableList()

        meta.forEach { metaQuestion ->
            questions.add(
                when(metaQuestion.questionType){
                    "SingleAnswerQuestion" -> SingleAnswerQuestion(activity!!, metaQuestion.questionBase as SingleAnswerQuestionBase)
                    "MultipleAnswerQuestion" -> MultipleAnswerQuestion(activity!!, metaQuestion.questionBase as MultipleAnswerQuestionBase)
                    else -> throw Exception("Unknown question type")
                }
            )
        }

        val questionPack = QuestionPack(activity!!, questions.toList())

        //scrollView.addView(questionPack)
    }

    //Для статистики
    override fun onResume() {
        super.onResume()

        val currentTime = Calendar.getInstance().timeInMillis
        lesson.time = currentTime
    }

    //Для статистики
    override fun onPause() {
        super.onPause()

        val currentTime = Calendar.getInstance().timeInMillis
        lesson.time = currentTime-lesson.time
        Thread().run {
            publishStat(lesson)
        }
    }

    //Для статистики
    private fun publishStat(stat: Statistic){
        val profileFragment = activity!!.supportFragmentManager.findFragmentByTag("profile") as ProfileFragment
        profileFragment.addStat(stat)
    }

    //Для статистики
    private fun setLesson(){
        val articleFragment = fragmentManager!!.findFragmentByTag("article") as ArticleFragment
        lesson = Lesson(articleFragment.lesson.name, null)
    }
}