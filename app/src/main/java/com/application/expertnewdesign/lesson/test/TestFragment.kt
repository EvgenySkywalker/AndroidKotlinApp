package com.application.expertnewdesign.lesson.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    private lateinit var lesson: Lesson

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        val articleFragment = fragmentManager!!.findFragmentByTag("article") as ArticleFragment
        lesson = articleFragment.lesson
        setTest()
    }

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

        scrollView.addView(questionPack)
        testBack.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        Thread().run{
            val currentTime = Calendar.getInstance().timeInMillis
            lesson.time = currentTime
        }
    }

    override fun onStop() {
        super.onStop()
        Thread().run {
            val currentTime = Calendar.getInstance().timeInMillis
            lesson.time = currentTime-lesson.time
            publishStat(lesson)
        }
    }

    private fun publishStat(stat: Statistic){
        val profileFragment = activity!!.supportFragmentManager.findFragmentByTag("profile") as ProfileFragment
        profileFragment.addStat(stat)
    }
}