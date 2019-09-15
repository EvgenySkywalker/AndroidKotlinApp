package com.application.expertnewdesign.lesson.test.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.R

class QuestionFragment (val question: Question): Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.question_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        //question_fragment.xml файл разметки
        //Тут можешь делать че хошь, чтобы вывести вопросик
    }

    fun getState(): Int{ //0-neutral 1-true 2-false
        return 0
    }
}