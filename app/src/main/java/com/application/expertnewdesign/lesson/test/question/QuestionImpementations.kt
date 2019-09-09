package com.application.expertnewdesign.lesson.test.question

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Build
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File

fun setDefaultBorder(view : View) {
    val border = ShapeDrawable()
    border.shape = RectShape()
    border.paint.color = Color.BLACK
    border.paint.strokeWidth = 1f
    border.paint.style = Paint.Style.STROKE
    view.background = border
}

open class MyLayout(context : Context) : LinearLayout(context){
    init{
        orientation = VERTICAL
        this.setPadding(9, 9, 9, 9)

        val border = ShapeDrawable()
        //border.shape = RoundRectShape(FloatArray(8) { 15f }, RectF(), FloatArray(8) { 10f })
        border.shape = RectShape()
        border.paint.color = Color.BLACK
        border.paint.strokeWidth = 18f
        border.paint.style = Paint.Style.STROKE
        background = border
    }

    fun setBorderColor(color : Int){
        val border = ShapeDrawable()
        border.shape = RectShape()
        border.paint.color = color
        border.paint.strokeWidth = 10f
        border.paint.style = Paint.Style.STROKE
        background = border
    }
}

//Класс определяющий вопрос, его состояния и цвета
abstract class Question(context: Context, val questionBase: QuestionBase) : MyLayout(context){
    enum class State{
        UNANSWERED, CORRECT, WRONG;

        fun getColor(): Int{
            return when(this){
                UNANSWERED -> 0xFF32B5E5.toInt()
                CORRECT -> 0xFF006400.toInt()
                WRONG -> 0xFFA11300.toInt()
            }
        }
    }

    enum class OptionState{
        CORRECT, WRONG, NEUTRAL, MISSED;

        fun getColor(): Int{
            return when(this){
                NEUTRAL -> Color.WHITE
                CORRECT -> 0xAA00CC00.toInt()
                WRONG -> 0xCCCC0000.toInt()
                MISSED -> 0xAA00CCCC.toInt()
            }
        }
    }

    private var questionTextView : TextView = TextView(context)
    protected var answerPanel  = LinearLayout(context)

    protected var state : State =
        State.UNANSWERED

    init {
        orientation = VERTICAL
        answerPanel.orientation = VERTICAL

        questionTextView.text = questionBase.questionText
        questionTextView.textSize = DEFAULT_TEXT_SIZE
        questionTextView.setPadding(10, 5, 10, 5)
        questionTextView.setTextColor(Color.BLACK)

        this.addView(questionTextView)
        this.addView(answerPanel)

        val newLayoutParams =  LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        newLayoutParams.setMargins(10, 10, 10, 10)
        layoutParams = newLayoutParams

       recolorBorder()
    }

    private fun recolorBorder(){
        setBorderColor(state.getColor())
    }

    fun check(){
        checkAnswer()
        recolorBorder()
    }

    abstract fun checkAnswer()
}

class QuestionPack(context: Context, private var questions : List<Question>) : LinearLayout(context) {
    private var sendButton = Button(context)

    init {
        orientation = VERTICAL

        for (question in questions)
            addView(question)

        sendButton.text = "Отправить ответ"
        sendButton.setBackgroundColor(0xFF32B5E5.toInt())
        sendButton.setOnClickListener {
            for (question in questions) {
                question.check()
            }
        }
        addView(sendButton)
    }

    constructor(context: Context) : this(context, emptyList())

    fun toXML(file : File, spacing: Int){
        //Формируем отступ
        var tabs = ""
        for(i in (0 until spacing))
            tabs += "\t"

        file.appendText("$tabs<QuestionPack>\n")

        for(question in questions){
            question.questionBase.toXML(file, spacing + 1)
        }

        file.appendText("$tabs</QuestionPack>\n")
    }
}

class SingleAnswerQuestion(context: Context, questionBase : SingleAnswerQuestionBase)
    : Question(context, questionBase){

    private var options : Array<RadioButton> = Array(questionBase.incorrectAnswers.size + 1) { RadioButton(context) }

    init {
        val mutableList = questionBase.incorrectAnswers.toMutableList()
        mutableList.add(questionBase.correctAnswer)
        mutableList.shuffle()

        val radioGroup = RadioGroup(context)
        for(i in (0 until mutableList.size)) {
            options[i].text = mutableList[i]
            options[i].layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            radioGroup.addView(options[i])
        }
        answerPanel.addView(radioGroup)
    }

    constructor(context: Context) : this(context, SingleAnswerQuestionBase())

    override fun checkAnswer(){
        questionBase as SingleAnswerQuestionBase

        for(i in (0 until options.size)){
            if(options[i].text == questionBase.correctAnswer){
                if(options[i].isChecked) {
                    state = State.CORRECT
                    options[i].setBackgroundColor(OptionState.CORRECT.getColor())
                } else {
                    options[i].setBackgroundColor(OptionState.MISSED.getColor())
                }
            } else {
                if(options[i].isChecked) {
                    state = State.WRONG
                    options[i].setBackgroundColor(OptionState.WRONG.getColor())
                } else {
                    options[i].setBackgroundColor(OptionState.NEUTRAL.getColor())
                }
            }
        }
    }
}

class MultipleAnswerQuestion(context: Context, questionBase : MultipleAnswerQuestionBase)
    : Question(context, questionBase){

    private val answerList : List<CheckBox> = List(questionBase.correctAnswers.size + questionBase.incorrectAnswers.size) { CheckBox(context) }

    init{
        val fullList = questionBase.correctAnswers.toMutableList()
        fullList.addAll(questionBase.incorrectAnswers)
        fullList.shuffle()

        for(i in (0 until fullList.size)){
            answerList[i].text = fullList[i]
            answerPanel.addView(answerList[i])
        }
    }

    constructor(context: Context) : this(context, MultipleAnswerQuestionBase())

    override fun checkAnswer() {
        questionBase as MultipleAnswerQuestionBase

        state = State.CORRECT
        for(option in answerList){
            if(questionBase.correctAnswers.contains(option.text.toString())) {
                if(option.isChecked){
                    option.setBackgroundColor(OptionState.CORRECT.getColor())
                } else {
                    option.setBackgroundColor(OptionState.MISSED.getColor())
                    state = State.WRONG
                }
            } else {
                if(option.isChecked){
                    option.setBackgroundColor(OptionState.WRONG.getColor())
                    state = State.WRONG
                } else {
                    option.setBackgroundColor(OptionState.NEUTRAL.getColor())
                }
            }
        }
    }
}

class DraggableQuestionOption(context: Context, value : String) : TextView(context){
    init{
        setDefaultBorder(this)
        textSize = DEFAULT_TEXT_SIZE

        text = value

        val newLayoutParams =  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        newLayoutParams.setMargins(0, 2, 0, 2)
        layoutParams = newLayoutParams

        setOnLongClickListener {
            val item = ClipData.Item(this.text)

            val dragData = ClipData(
                this.text,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item)

            if (Build.VERSION.SDK_INT >= 24) {
                it.startDragAndDrop(dragData, DragShadowBuilder(this), null, 0)
            } else {
                @Suppress("DEPRECATION")
                it.startDrag(dragData, DragShadowBuilder(this), null, 0)
            }

            true
        }
    }

    constructor(context: Context) : this(context, "")
}

class QuestionOptionReceiver(context: Context, private val answer : String) : TextView(context){

    init{
        colorBorder(Color.BLACK)
        gravity = Gravity.CENTER
        textSize = DEFAULT_TEXT_SIZE

        setOnDragListener{ _: View, dragEvent: DragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    colorBorder(Color.GREEN, 3f)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    colorBorder(Color.BLACK)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    // Gets the item containing the dragged data
                    val item: ClipData.Item = dragEvent.clipData.getItemAt(0)

                    // Gets the text data from the item.
                    val dragData = item.text

                    colorBorder(Color.BLACK)
                    this.text = dragData
                    true
                }

                else -> true
            }
        }
    }

    constructor(context: Context) : this(context, "")

    private fun colorBorder(color : Int, width : Float = 1f){
        val border = ShapeDrawable()
        border.shape = RectShape()
        border.paint.color = color
        border.paint.strokeWidth = width
        border.paint.style = Paint.Style.STROKE
        background = border
    }

    fun isCorrect() : Boolean = answer == text
}

class DragQuestion(context: Context, questionBase : DragQuestionBase)
    : Question(context, questionBase){

    private val answers= Array(questionBase.rightColumn.size) {
        QuestionOptionReceiver(
            context,
            questionBase.rightColumn[it]
        )
    }

    constructor(context: Context) : this(context, DragQuestionBase())

    override fun checkAnswer() {
        state = State.CORRECT
        for(answer in answers){
            if(answer.isCorrect()){
                answer.setBackgroundColor(OptionState.CORRECT.getColor())
            } else {
                state = State.WRONG
                answer.setBackgroundColor(OptionState.WRONG.getColor())
            }
        }
    }

    init{
        val table = TableLayout(context)
        table.isStretchAllColumns = true
        for(i in (0 until questionBase.leftColumn.size)){
            val row = TableRow(context)

            val left = TextView(context)
            left.text = questionBase.leftColumn[i]
            left.textSize = DEFAULT_TEXT_SIZE
            left.gravity = Gravity.CENTER
            setDefaultBorder(left)
            row.addView(left)

            row.addView(answers[i])

            table.addView(row)
        }

        val newLayoutParams =  LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        newLayoutParams.setMargins(0, 5, 0, 5)
        table.layoutParams = newLayoutParams

        answerPanel.addView(table)

        //Добавление перетягиваемых опций
        val fullList = questionBase.rightColumn.toMutableList()
        fullList.addAll(questionBase.otherOptions)
        fullList.shuffle()

        for(value in fullList){
            answerPanel.addView(DraggableQuestionOption(context, value))
        }
    }

    private fun setDefaultBorder(view : View){
        val border = ShapeDrawable()
        border.shape = RectShape()
        border.paint.color = Color.BLACK
        border.paint.strokeWidth = 1f
        border.paint.style = Paint.Style.STROKE
        view.background = border
    }
}