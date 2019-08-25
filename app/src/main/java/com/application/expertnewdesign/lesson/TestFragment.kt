package com.application.expertnewdesign.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.R
import com.application.expertnewdesign.question.*
import kotlinx.android.synthetic.main.test_fragment.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File
import java.lang.Exception

class TestFragment(val path: String) : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        setTest()
    }

    private fun setTest() {
        val file = File(path, "questions.json")

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
}