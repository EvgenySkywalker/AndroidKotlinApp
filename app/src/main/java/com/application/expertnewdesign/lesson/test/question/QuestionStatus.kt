package com.application.expertnewdesign.lesson.test.question

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.application.expertnewdesign.R


enum class State{
    INCORRECT, PARTIALLY_CORRECT, CORRECT, NONE
}

class QuestionStatus(context: Context, attributeSet: AttributeSet) : TextView(context, attributeSet){

    val CORRECT : IntArray = IntArray(1){R.attr.correct}
    val PARTIALLY_CORRECT = IntArray(1){R.attr.partially_correct}
    val INCORRECT = IntArray(1){R.attr.incorrect}

    private var state = State.NONE

    fun setState(state : State){
        this.state = state
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {

        /*when(state){

            State.CORRECT -> {
                val drawable = super.onCreateDrawableState(extraSpace + 1)
                View.mergeDrawableStates(drawable, CORRECT)
                return drawable
            }

            State.PARTIALLY_CORRECT -> {
                val drawable = super.onCreateDrawableState(extraSpace + 1)
                View.mergeDrawableStates(drawable, PARTIALLY_CORRECT)
                return drawable
            }

            State.INCORRECT -> {
                val drawable = super.onCreateDrawableState(extraSpace + 1)
                View.mergeDrawableStates(drawable, INCORRECT)
                return drawable
            }

            State.NONE -> {
                return super.onCreateDrawableState(extraSpace)
            }
        }*/

        if(state == State.CORRECT){
            val drawable = super.onCreateDrawableState(extraSpace + 1)
            View.mergeDrawableStates(drawable, CORRECT)
            return drawable
        }

        if(state == State.PARTIALLY_CORRECT){
            val drawable = super.onCreateDrawableState(extraSpace + 1)
            View.mergeDrawableStates(drawable, PARTIALLY_CORRECT)
            return drawable
        }

        if(state == State.INCORRECT){
            val drawable = super.onCreateDrawableState(extraSpace + 1)
            View.mergeDrawableStates(drawable, INCORRECT)
            return drawable
        }

        return super.onCreateDrawableState(extraSpace)

    }
}