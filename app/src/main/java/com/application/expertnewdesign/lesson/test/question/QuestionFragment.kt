package com.application.expertnewdesign.lesson.test.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.R
import kotlinx.android.synthetic.main.question_fragment.*

class QuestionFragment (private val question: Question): Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.question_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        //scrollView.addView(question)
        root.addView(question)
    }

    override fun onStop() {
        super.onStop()
        //scrollView.removeView(question)
        root.removeView(question)
    }
}