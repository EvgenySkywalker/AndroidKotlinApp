package com.application.expertnewdesign.lesson.test.question

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import com.application.expertnewdesign.R
import kotlinx.android.synthetic.main.multiple_answer_question_modified.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.android.synthetic.main.question_ege.view.*
import kotlinx.android.synthetic.main.question_fragment.view.*
import kotlinx.android.synthetic.main.single_answer_question_modified.view.*
import java.io.File
import java.util.*
import java.util.jar.Attributes

fun String.padAnswer(toLength : Int) : String{
    if(length == toLength)
        return this

    if(length > toLength){
        return this.substring(0, toLength)
    }

    val diff = toLength - length
    var appendage = ""
    for(i in 0 until diff)
        appendage += " "
    return this + appendage
}

class Filters{

    companion object {
        val UniqueFilter = InputFilter { source, _, _, dest, _, _ ->
            var resultString = ""
            source.forEach {
                if(!dest.contains(it))
                    resultString += it
            }
            resultString
        }
    }


}


fun getEnding(length : Int) : String{
    return if(length in 10..19){
        "ел"
    } else {
        when(length % 10){
            1 -> "ло"
            2, 3, 4 -> "ла"
            0, 5, 6, 7, 8, 9 -> "ел"
            else -> ""
        }
    }
}

abstract class MyLayout(context: Context, inflattable : Int) : LinearLayout(context){
    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(ViewGroup.inflate(context, inflattable, null))
    }
}

abstract class Question(context: Context, inflattable: Int, val questionBase: QuestionBase, val imageFile: File?)
    : MyLayout(context, inflattable) {

    var earnedPoints = 0

    init {
        //Setting up status
        setStatusText()

        //Setting up question text
        questionText.text = questionBase.questionText

        //Setting up image
        if(imageFile != null){
            image.setImageBitmap(BitmapFactory.decodeFile(imageFile.absolutePath))
        }
    }

    private fun setStatusText(){
        status.text = "${earnedPoints}/${questionBase.maxGrade}"
    }

    fun check(){
        grade()
        setStatusText()
        status.setState(
            when(earnedPoints){
                0 -> State.INCORRECT
                questionBase.maxGrade -> State.CORRECT
                else -> State.PARTIALLY_CORRECT
            }
        )
    }

    abstract fun grade()
}

abstract class QuestionEGE(context: Context, questionBase: QuestionBase, image: File?)
    : Question(context, R.layout.question, questionBase, image)
{
    val answer = EditText(context)

    init {
        answer.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        answer.hint = "Введите ответ"
        initInputType()
        answerHolder.addView(answer)
    }

    abstract fun initInputType()
}

class OneWordQuestionEGE(context: Context, questionBase: OneWordQuestionEGE_Base, image: File?)
    : QuestionEGE(context, questionBase, image)
{
    override fun initInputType() {
        answer.inputType = InputType.TYPE_CLASS_TEXT
    }

    override fun grade() {
        val correctAnswers = (questionBase as OneWordQuestionEGE_Base).correctAnswers
        val answer = answer.text.toString()

        var isCorrect = false
        correctAnswers.forEach { ans ->
            if(ans.toLowerCase(Locale.ROOT) == answer.toLowerCase(Locale.ROOT)){
                isCorrect = true
            }
        }

        earnedPoints = if(isCorrect) questionBase.maxGrade else 0
    }
}

class MultipleAnswerQuestionEGE(context: Context, questionBase: MultipleAnswerQuestionEGE_Base, image: File?)
    : QuestionEGE(context, questionBase, image)
{
    override fun initInputType() {
        val correctAnswer = (questionBase as MultipleAnswerQuestionEGE_Base).correctAnswer

        answer.inputType = InputType.TYPE_CLASS_NUMBER
        answer.hint = "Введите ${correctAnswer.length} чис${getEnding(correctAnswer.length)}"

        answer.filters = arrayOf(Filters.UniqueFilter, InputFilter.LengthFilter(correctAnswer.length))
    }

    override fun grade() {
        val correctAnswer = (questionBase as MultipleAnswerQuestionEGE_Base).correctAnswer
        val answer = answer.text.toString().padAnswer(correctAnswer.length)

        var mistakes = 0
        answer.forEach {
            if(!correctAnswer.contains(it)){
                ++mistakes
                if(it != ' ')
                    ++mistakes
            }
        }

        val result = questionBase.maxGrade - mistakes
        earnedPoints = if(result >= 0) result else 0
    }
}

class SequenceQuestionEGE(context: Context, questionBase: SequenceQuestionEGE_Base, image: File?)
    : QuestionEGE(context, questionBase, image)
{
    override fun initInputType() {
        answer.inputType = InputType.TYPE_CLASS_NUMBER

        val correctAnswer = (questionBase as SequenceQuestionEGE_Base).correctAnswer

        answer.hint = "Введите ${correctAnswer.length} чис${getEnding(correctAnswer.length)}"

        val filter = InputFilter { source, _, _, dest, _, _ ->
            var resultString = ""
            source.forEach {
                if(!dest.contains(it) && correctAnswer.contains(it))
                    resultString += it
            }
            resultString
        }
        answer.filters = arrayOf(filter)
    }

    override fun grade() {
        val correctAnswer = (questionBase as SequenceQuestionEGE_Base).correctAnswer
        val answer = answer.text.toString()
        earnedPoints = if(correctAnswer == answer){
            questionBase.maxGrade
        } else {
            0
        }
    }
}

class MatchQuestionEGE(context: Context, questionBase: MatchQuestionEGE_Base, image: File?)
    : QuestionEGE(context, questionBase, image)
{
    override fun initInputType() {
        answer.inputType = InputType.TYPE_CLASS_NUMBER

        val correctAnswer = (questionBase as MatchQuestionEGE_Base).correctAnswer

        answer.hint = "Введите ${correctAnswer.length} чис${getEnding(correctAnswer.length)}"

        answer.filters = arrayOf(InputFilter.LengthFilter(correctAnswer.length))
    }

    override fun grade() {
        val correctAnswer = (questionBase as MatchQuestionEGE_Base).correctAnswer
        val answer = answer.text.toString().padAnswer(correctAnswer.length)

        var mistakes = 0
        answer.forEachIndexed { index, c ->
            if(c != correctAnswer[index])
                ++mistakes
        }

        val result = questionBase.maxGrade - mistakes
        earnedPoints = if(result >= 0) result else 0
    }
}

class PairMatchQuestionEGE(context: Context, questionBase: PairMatchQuestionEGE_Base, image: File?)
    : QuestionEGE(context, questionBase, image)
{
    override fun initInputType() {
        answer.inputType = InputType.TYPE_CLASS_NUMBER
        answer.hint = "Введите 4 числа"

        val filter = InputFilter { source, _, _, dest, _, _ ->
            var resultString = ""
            source.forEach {
                if(!dest.contains(it))
                    resultString += it
            }
            resultString
        }
        answer.filters = arrayOf(filter)
    }

    override fun grade() {
        val correctAnswer = (questionBase as PairMatchQuestionEGE_Base).correctAnswer
        val answer = answer.text.toString().padAnswer(correctAnswer.length)

        fun checkPair(pair : String, correctPair : String) : Boolean{
            pair.forEach {
                if(!correctPair.contains(it))
                    return false
            }
            return true
        }

        var result = 0
        if(checkPair(answer.substring(0, 2), correctAnswer.substring(0,2)))
            ++result

        if(checkPair(answer.substring(2, 4), correctAnswer.substring(2,4)))
            ++result

        earnedPoints = result
    }
}

// Modified section

abstract class QuestionModified(){
}

class SingleAnswerQuestionModified(context: Context, questionBase: SingleAnswerQuestionBase, image: File?)
    : Question(context, R.layout.question, questionBase, image)
{

    val radioButtons = Array(questionBase.incorrectAnswers.size + 1){RadioButton(context)}

    init{
        val allOptions : MutableList<String> = emptyList<String>().toMutableList()
        allOptions.add(questionBase.correctAnswer)
        allOptions.addAll(questionBase.incorrectAnswers)
        allOptions.shuffle()

        val radioGroup = RadioGroup(context)

        allOptions.forEachIndexed { index, value ->
            radioButtons[index].text = value
            radioGroup.addView(radioButtons[index])
        }

        answerHolder.addView(radioGroup)
    }


    override fun grade() {
        val correctAnswer = (questionBase as SingleAnswerQuestionBase).correctAnswer

        var isCorrect = false
        radioButtons.forEach {
            if(it.isChecked && it.text.toString() == correctAnswer){
                isCorrect = true
            }
        }

        earnedPoints = if(isCorrect) questionBase.maxGrade else 0
    }

}

class MultipleAnswerQuestion(context: Context, questionBase: MultipleAnswerQuestionBase, image: File?)
    : Question(context, R.layout.question, questionBase, image)
{
    private val checkBoxes = Array(questionBase.correctAnswers.size + questionBase.incorrectAnswers.size){CheckBox(context)}

    init{
        val allOptions = emptyList<String>().toMutableList()
        allOptions.addAll(questionBase.correctAnswers)
        allOptions.addAll(questionBase.incorrectAnswers)
        allOptions.shuffle()

        allOptions.forEachIndexed { index, s ->
            checkBoxes[index].text = s
            answerHolder.addView(checkBoxes[index])
        }
    }

    override fun grade() {
        val correctAnswers = (questionBase as MultipleAnswerQuestionBase).correctAnswers

        var mistakes = 0
        checkBoxes.forEach { checkBox ->
            if(checkBox.isChecked){
                if(!correctAnswers.contains(checkBox.text.toString()))
                    ++mistakes
            } else {
                if(correctAnswers.contains(checkBox.text.toString()))
                    ++mistakes
            }
        }

        val result = questionBase.maxGrade - mistakes
        earnedPoints = if(result > 0) result else 0
    }

}

/*fun setDefaultBorder(view : View) {
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

abstract class Question(context: Context, val questionBase: QuestionBase, image: File?) : LinearLayout(context){
    val status = TextView(context)
    val scroll = ScrollView(context)
    val questionText = TextView(context)
    val imageView = ImageView(context)
    val answer = EditText(context)

    var earnedPoints = 0

    init {
        orientation = VERTICAL

        status.text = questionBase.maxGrade.toString()
        addView(status)

        val scrollLayout = LinearLayout(context)
        scroll.addView(scrollLayout)
        addView(scroll)


        questionText.text = questionBase.questionText
        scrollLayout.addView(questionText)

        if (image != null){
            imageView.setImageBitmap(BitmapFactory.decodeFile(image.absolutePath))

            scrollLayout.addView(imageView)

            imageView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true
        }

        answer.hint = "Введите ответ"
        addView(answer)
    }

    fun check(){

    }

    abstract fun checkAnswer()
}

//Класс определяющий вопрос, его состояния и цвета
abstract class QuestionModified(context: Context, questionBase: QuestionBase, image : File? = null) : Question(context, questionBase, image){
    enum class State(val value : Int){
        UNANSWERED(0), CORRECT(1), WRONG(2);

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

    protected var state : State = State.UNANSWERED
    protected var answerPanel  = LinearLayout(context)
    init {
        addView(answerPanel)
    }


    /*private var questionTextView : TextView = TextView(context)
    private val pointsView = TextView(context)
    protected var answerPanel  = LinearLayout(context)


    private fun refreshPoints(){
        pointsView.text = "$earnedPoints/${questionBase.maxGrade}"
    }

    init {
        orientation = VERTICAL
        answerPanel.orientation = VERTICAL

        questionTextView.text = questionBase.questionText
        questionTextView.textSize = DEFAULT_TEXT_SIZE
        questionTextView.setPadding(10, 5, 10, 5)
        questionTextView.setTextColor(Color.BLACK)

        addView(pointsView)
        addView(questionTextView)

        if (image != null){
            val imageView = ImageView(context)
            imageView.setImageBitmap(BitmapFactory.decodeFile(image.absolutePath))
            //imageView.layoutParams.width =

            addView(imageView)

            imageView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true
        }

        addView(answerPanel)
        answerPanel.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val layoutParams =  LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(10, 10, 10, 10)
        this.layoutParams = layoutParams

        recolorBorder()
        refreshPoints()
    }

    private fun recolorBorder(){
        //setBorderColor(state.getColor())
    }

    /*override fun check(){
        checkAnswer()
        recolorBorder()
        refreshPoints()
    }*/

    fun getState() : Int {
        return state.value
    }*/
}

abstract class QuestionEGE(context: Context, questionBase: QuestionBase, image : File? = null) : Question(context, questionBase, image){



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

    /*fun toXML(file : File, spacing: Int){
        //Формируем отступ
        var tabs = ""
        for(i in (0 until spacing))
            tabs += "\t"

        file.appendText("$tabs<QuestionPack>\n")

        for(question in questions){
            question.questionBase.toXML(file, spacing + 1)
        }

        file.appendText("$tabs</QuestionPack>\n")
    }*/
}

class SingleAnswerQuestion(context: Context, questionBase : SingleAnswerQuestionBase, image : File?)
    : QuestionModified(context, questionBase, image){

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

    constructor(context: Context) : this(context, SingleAnswerQuestionBase(), null)

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

class MultipleAnswerQuestion(context: Context, questionBase : MultipleAnswerQuestionBase, image : File?)
    : QuestionModified(context, questionBase, image){

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

    constructor(context: Context) : this(context, MultipleAnswerQuestionBase(), null)

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

class OneWordQuestion(context: Context, questionBase : OneWordQuestionBase, image : File?)
    : QuestionModified(context, questionBase, image){

    val answer2 = EditText(context)
    val correctAnswer = questionBase.correctAnswer

    init {
        addView(answer2)
    }


    override fun checkAnswer() {
        state = if(answer2.text.toString().toLowerCase() == correctAnswer)
            State.CORRECT
        else
            State.WRONG
    }

}

class ChronologicalQuestion(context: Context, questionBase : ChronologicalQuestionBase, image : File?)
    : QuestionModified(context, questionBase, image){

    val correctSequence = emptyList<Int>().toMutableList()
    val spinners = emptyList<Spinner>().toMutableList()

    init{
        val shuffled = questionBase.correctOrder.toMutableList()
        shuffled.shuffle()
        shuffled.forEach{
            correctSequence.add(questionBase.correctOrder.indexOf(it)+1)
            spinners.add(Spinner(context))
        }

        val numbers = emptyList<Int>().toMutableList()
        for(i in questionBase.correctOrder.indices)
            numbers.add(i + 1)

        val table = TableLayout(context)
        table.isStretchAllColumns = true
        shuffled.forEachIndexed { index, i ->
            val row = TableRow(context)

            val left = TextView(context)
            left.text = i
            left.textSize = DEFAULT_TEXT_SIZE
            left.gravity = Gravity.CENTER
            setDefaultBorder(left)
            row.addView(left)

            spinners[index] = Spinner(context)
            spinners[index].adapter = ArrayAdapter<Int>(context, android.R.layout.simple_spinner_item, numbers)
            row.addView(spinners[index])

            table.addView(row)
        }
        addView(table)
    }

    override fun checkAnswer() {
        spinners.forEachIndexed{ index, spinner ->
            if(spinner.selectedItem != correctSequence[index]){
                state = State.WRONG
            }
        }
        state = State.CORRECT
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

class DragQuestion(context: Context, questionBase : MatchQuestionBase, image : File?)
    : QuestionModified(context, questionBase, image){

    private val answers= Array(questionBase.pairs.size) {
        QuestionOptionReceiver(
            context,
            questionBase.pairs[it].second
        )
    }

    constructor(context: Context) : this(context, MatchQuestionBase(), null)

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
        for(i in (0 until questionBase.pairs.size)){
            val row = TableRow(context)

            val left = TextView(context)
            left.text = questionBase.pairs[i].first
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
        val fullList = questionBase.pairs.map {
            it.second
        }.toMutableList()
        fullList.addAll(questionBase.incorrectAnswers)
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


abstract class EGE_Question(context: Context, questionBase : QuestionBase, image : File?)
    : QuestionModified(context, questionBase, image)
{
    val answer2 = EditText(context)

    init {
        answer2.hint = "Введите ответ"
        answerPanel.addView(answer2)
    }
}

class OneWordQuestionEGE(context: Context, questionBase : OneWordQuestionEGE_Base, image : File?)
    : EGE_Question(context, questionBase, image)
{
    val correctAnswer = questionBase.correctAnswer

    override fun checkAnswer() {
        if(answer.text.toString().toLowerCase() == correctAnswer){
            earnedPoints = 1
            state = State.CORRECT
        } else {
            earnedPoints = 0
            state = State.WRONG
        }
    }

}

class MultipleAnswerQuestionEGE2(context: Context, questionBase : MultipleAnswerQuestionEGE_Base, image : File?)
    :QuestionEGE(context, questionBase, image)
{
    override fun checkAnswer() {

    }

}

class MultipleAnswerQuestionEGE(context: Context, questionBase : MultipleAnswerQuestionEGE_Base, image : File?)
    : EGE_Question(context, questionBase, image)
{
    val correctAnswer = questionBase.correctAnswer

    init {
        //editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthofEditText)});
        val filter = InputFilter.LengthFilter(correctAnswer.length)
        answer.filters = arrayOf<InputFilter>(filter)
    }

    override fun checkAnswer() {
        var mistakes = 0
        val givenAnswer = answer.text.toString()
        givenAnswer.forEach {
            if(!correctAnswer.contains(it))
                ++mistakes
        }
        mistakes += correctAnswer.length - givenAnswer.length


        when(questionBase.maxGrade){
            1 -> {
                if(mistakes == 0){
                    earnedPoints = 1
                    state = State.CORRECT
                } else {
                    earnedPoints = 0
                    state = State.WRONG
                }
            }

            2 -> {
                when(mistakes){
                    0 -> {
                        earnedPoints = 2
                        state = State.CORRECT
                    }

                    1 -> {
                        earnedPoints = 1
                        state = State.WRONG
                    }

                    else -> {
                        earnedPoints = 0
                        state = State.WRONG
                    }
                }
            }

            else -> throw Exception("Неизвестная стратегия оценивания")
        }
    }

}

class SequenceQuestionEGE(context: Context, questionBase : SequenceQuestionEGE_Base, image : File?)
    : EGE_Question(context, questionBase, image)
{
    val correctAnswer = questionBase.correctAnswer

    init {
        //editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthofEditText)});
        val filter = InputFilter.LengthFilter(correctAnswer.length)
        answer.filters = arrayOf<InputFilter>(filter)
    }

    override fun checkAnswer() {
        var mistakes = 0
        val givenAnswer = answer.text.toString()
        givenAnswer.forEachIndexed { index, char ->
            if(char != correctAnswer[index])
                ++mistakes
        }
        mistakes += correctAnswer.length - givenAnswer.length


        when(questionBase.maxGrade){
            1 -> {
                if(mistakes == 0){
                    earnedPoints = 1
                    state = State.CORRECT
                } else {
                    earnedPoints = 0
                    state = State.WRONG
                }
            }

            3 -> {
                when(mistakes){
                    0 -> {
                        earnedPoints = 3
                        state = State.CORRECT
                    }

                    1 -> {
                        earnedPoints = 2
                        state = State.WRONG
                    }

                    else -> {
                        earnedPoints = 0
                        state = State.WRONG
                    }
                }
            }

            else -> throw Exception("Неизвестная стратегия оценивания")
        }



    }

}

class MatchQuestionEGE(context: Context, questionBase : MatchQuestionEGE_Base, image : File?)
    : EGE_Question(context, questionBase, image)
{
    val correctAnswer = questionBase.correctAnswer

    init {
        //editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthofEditText)});
        val filter = InputFilter.LengthFilter(correctAnswer.length)
        answer.filters = arrayOf<InputFilter>(filter)
    }


    override fun checkAnswer() {
        var mistakes = 0
        val givenAnswer = answer.text.toString()
        givenAnswer.forEachIndexed { index, char ->
            if(char != correctAnswer[index])
                ++mistakes
        }
        mistakes += correctAnswer.length - givenAnswer.length

        when(mistakes){
            0 -> {
                earnedPoints = 2
                state = State.CORRECT
            }

            1 -> {
                earnedPoints = 1
                state = State.WRONG
            }

            else -> {
                earnedPoints = 0
                state = State.WRONG
            }
        }

    }

}

class PairMatchQuestionEGE(context: Context, questionBase : PairMatchQuestionEGE_Base, image : File?)
    : EGE_Question(context, questionBase, image)
{
    val correctAnswer = questionBase.correctAnswer

    init {
        //editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthofEditText)});
        val filter = InputFilter.LengthFilter(correctAnswer.length)
        answer.filters = arrayOf<InputFilter>(filter)
    }

    override fun checkAnswer() {
        fun checkPair(pair : String, correctPair : String) : Boolean{
            pair.forEach {
                if(!correctPair.contains(it))
                    return false
            }
            return true
        }


        var paddedAnswer = answer.text.toString()
        for(i in (0..(correctAnswer.length - answer.text.toString().length)))
            paddedAnswer += " "

        var result = 0
        if(checkPair(paddedAnswer.substring(0, 2), correctAnswer.substring(0,2)))
            ++result

        if(checkPair(paddedAnswer.substring(2, 4), correctAnswer.substring(2,4)))
            ++result

        earnedPoints = result

        state =  if(earnedPoints != questionBase.maxGrade)
            State.WRONG
        else
            State.CORRECT




    }

}*/

/*abstract class Question(context: Context, val questionBase: QuestionBase, val image: File?)
    : LinearLayout(context){

    //Views
    val status = TextView(context)
    val scroll = ScrollView(context)
    val questionText = TextView(context)
    val imageView = ImageView(context)

    var earnedPoints = 0

    private fun setStatusText(){
        status.text = "$earnedPoints/${questionBase.maxGrade}"
    }

    init {
        orientation = VERTICAL

        //Статус
        setStatusText()
        addView(status)

        //ScrollLayout
        val scrollLayout = LinearLayout(context)
        scrollLayout.orientation = VERTICAL
        scroll.addView(scrollLayout)
        addView(scroll)

        //QuestionText
        questionText.text = questionBase.questionText
        scrollLayout.addView(questionText)

        //Image
        if (image != null){
            imageView.setImageBitmap(BitmapFactory.decodeFile(image.absolutePath))

            imageView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true

            scrollLayout.addView(imageView)
        }
    }

    abstract fun check()
}*/
