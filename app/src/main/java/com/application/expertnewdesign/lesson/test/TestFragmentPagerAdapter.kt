package com.application.expertnewdesign.lesson.test

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.application.expertnewdesign.R
import com.application.expertnewdesign.lesson.test.question.Question
import com.application.expertnewdesign.lesson.test.question.QuestionFragment

class TestFragmentPagerAdapter(fm: FragmentManager,
                               private val questions: List<Question>,
                               private val dots: LinearLayout,
                               private val context: Context) : FragmentPagerAdapter(fm) {

    var dotsList: MutableList<ImageView> = arrayListOf()

    init {
        questions.forEach {
            val dotImage = ImageView(context)
            if(it != questions[0]) {
                dotImage.setImageResource(R.drawable.ic_dot)
            }else{
                dotImage.setImageResource(R.drawable.ic_dot_selected)
            }
            dotsList.add(dotImage)
            dots.addView(dotImage)
        }
    }

    override fun getCount(): Int {
        return questions.size
    }

    override fun getItem(position: Int): Fragment {
        return QuestionFragment(questions[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return questions[position].toString()
    }
}