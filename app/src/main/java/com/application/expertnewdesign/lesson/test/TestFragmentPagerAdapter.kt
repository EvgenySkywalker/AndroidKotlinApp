package com.application.expertnewdesign.lesson.test

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.application.expertnewdesign.R
import com.application.expertnewdesign.lesson.test.question.Question
import com.application.expertnewdesign.lesson.test.question.QuestionFragment
import com.application.expertnewdesign.profile.QuestionObject

class TestFragmentPagerAdapter(fm: FragmentManager,
                               private val questions: List<Question>,
                               private val dots: LinearLayout,
                               private val context: Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var dotsList: MutableList<ImageView> = arrayListOf()
    var lastPosition: Int = 0
    var lastState: Drawable? = null

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

    fun getStates(): List<QuestionObject>{
        val states: MutableList<QuestionObject> = arrayListOf()
        for((index, element) in questions.withIndex()){
            element.check()
            val state = element.getState()
            when(state){
                0->{
                    dotsList[index].setImageResource(R.drawable.ic_dot)
                }
                1->{
                    dotsList[index].setImageResource(R.drawable.ic_dot_right)
                }
                2->{
                    dotsList[index].setImageResource(R.drawable.ic_dot_wrong)
                }
            }
            if(index == lastPosition){
                lastState = dotsList[index].drawable
            }
            states.add(QuestionObject(element.questionBase.questionID, state))
        }
        return states
    }
}