package com.application.expertnewdesign.lesson.test

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.application.expertnewdesign.BASE_URL
import com.application.expertnewdesign.R
import com.application.expertnewdesign.lesson.test.question.*
import com.application.expertnewdesign.profile.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.test_fragment.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query
import java.io.File
import java.lang.Exception
import android.os.Handler
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast


interface TestStatAPI{
    @PUT("updateSelfAnswers")
    fun putTestInfo(@Header("Authorization") token: String,
                    @Query("course") subject: String,
                    @Query("subject") topic: String,
                    @Query("lesson") lesson: String,
                    @Query("answers") array: String): Call<ResponseBody>
}

class TestFragment(val path: String, var isFinal: Boolean = false, val time: Long = 1200000) : Fragment(){

    //Таймер
    private var startTime: Long = 0
    val timerHandler = Handler()
    private val timerRunnable = object : Runnable {

        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            var seconds = ((time-millis) / 1000).toInt()
            if(seconds > 0) {
                val minutes = seconds / 60
                seconds %= 60

                if (timer != null) {
                    timer.text = String.format("%02d:%02d", minutes, seconds)
                    if(seconds / 60 < 3) {
                        timer.setTextColor(Color.RED)
                    }
                }

                timerHandler.postDelayed(this, 500)
            }else{
                val resultList = (viewPager.adapter as TestFragmentPagerAdapter).getGrades()
                putTestStat(TestObject(path, resultList))
                testToolbar.menu.findItem(R.id.finish).isVisible = false
                if (timer != null) {
                    timer.setTextColor(Color.GRAY)
                    timer.text = String.format("%02d:%02d", 0, 0)
                }
                showResult(resultList)
                isFinal = false
            }
        }
    }

    lateinit var questionList: List<Question>
    var adapter: TestFragmentPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        setTest()

        /*val s = """ [{"questionType":"SingleAnswerQuestion","questionData":{"questionBasic":{"questionID":1,"questionText":"SAQ"},"correctAnswer":"1","incorrectAnswers":["2","3"]}},{"questionType":"MultipleAnswerQuestion","questionData":{"questionBasic":{"questionID":2,"questionText":"MAQ"},"correctAnswers":["1","2"],"incorrectAnswers":["3","4"]}},{"questionType":"OneWordQuestion","questionData":{"questionBasic":{"questionID":3,"questionText":"Word"},"correctAnswer":"correct"}},{"questionType":"ChronologicalQuestion","questionData":{"questionBasic":{"questionID":4,"questionText":""},"correctOrder":["1","2","3"]}},{"questionType":"MatchQuestion","questionData":{"questionBasic":{"questionID":5,"questionText":""},"pairs":[{"first":"l1","second":"r1"},{"first":"l2","second":"r2"}],"incorrectAnswers":["e1","e2"]}}] """
        //Сюды класть вопросики
        val meta = Json(JsonConfiguration.Stable).parse(QuestionMetadata.serializer().list, s)
        val list = meta.map {
            when(it.questionType){
                "SingleAnswerQuestion" -> SingleAnswerQuestion(context!!, it.questionBase as SingleAnswerQuestionBase)
                "MultipleAnswerQuestion" -> MultipleAnswerQuestion(context!!, it.questionBase as MultipleAnswerQuestionBase)
                "MatchQuestion" -> DragQuestion(context!!, it.questionBase as MatchQuestionBase)
                "OneWordQuestion" -> OneWordQuestion(context!!, it.questionBase as OneWordQuestionBase)
                "ChronologicalQuestion" -> ChronologicalQuestion(context!!, it.questionBase as ChronologicalQuestionBase)
                else -> SingleAnswerQuestion(context!!, SingleAnswerQuestionBase("placeholder", 999, "asd", listOf("1", "2")))
            }
        }*/
        if(isFinal){
            setTimer()
        }
    }

    private fun setToolbar(){
        testToolbar.inflateMenu(R.menu.test)
        testToolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem!!.itemId){
                R.id.finish->{
                    val resultList = adapter!!.getGrades()
                    if(isFinal) {
                        putTestStat(TestObject(path, resultList))
                        timerHandler.removeCallbacks(timerRunnable)
                        if (timer != null) {
                            timer.setTextColor(Color.GRAY)
                            timer.text = String.format("%02d:%02d", 0, 0)
                        }
                        menuItem.isVisible = false
                        isFinal = false
                    }
                    showResult(resultList)
                }
                else->{
                    super.onOptionsItemSelected(menuItem)
                }
            }
            true
        }
        testBack.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    private fun setTest(){
        setQuestions()
        adapter = TestFragmentPagerAdapter(childFragmentManager, questionList, dots_layout, context!!)
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                val testAdapter = adapter
                if(testAdapter!!.lastState != null) {
                    testAdapter.dotsList[testAdapter.lastPosition].setImageDrawable(testAdapter.lastState)
                }else{
                    testAdapter.dotsList[testAdapter.lastPosition].setImageResource(R.drawable.ic_dot)
                }
                testAdapter.lastPosition = position
                testAdapter.lastState = testAdapter.dotsList[position].drawable
                testAdapter.dotsList[position].setImageResource(R.drawable.ic_dot_selected)
                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(container.windowToken, 0)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun showResult(questions: List<QuestionObject>){
        var earned = 0
        var max = 0
        questions.forEach {
            earned += it.status
            max += it.max
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Результат:")
            .setMessage("Вы набрали $earned из $max возможных")
            .setNeutralButton("Ок") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
        val neutralButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL)
        val parent = neutralButton.parent as LinearLayout
        parent.gravity = Gravity.CENTER_HORIZONTAL
        parent.setPadding(0, 0, 0, 20)
        val leftSpacer = parent.getChildAt(1)
        leftSpacer.visibility = GONE
    }

    //С этим делай че хошь. Будет кайф, если будет выплевывать вопросики
    private fun setQuestions(){
        val src = File(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).append("questions.json").toString())

        val directory = File(StringBuilder(context!!.getExternalFilesDir(null).toString()).append(path).toString())

        val meta = Json(JsonConfiguration.Stable).parse(QuestionMetadata.serializer().list, src.readText())

        val questions = emptyList<Question>().toMutableList()

        meta.forEach { metaQuestion ->
            var image : File? = null
            for(file in directory.listFiles()!!){
                if(file.name.split(".")[0] == metaQuestion.questionBase.questionID.toString()){
                    image = file
                    break
                }
            }

            questions.add(
                when(metaQuestion.questionType){
                    "SingleAnswerQuestion" -> SingleAnswerQuestionModified(context!!, metaQuestion.questionBase as SingleAnswerQuestionBase, image)
                    "MultipleAnswerQuestion" -> MultipleAnswerQuestion(context!!, metaQuestion.questionBase as MultipleAnswerQuestionBase, image)
                    //"MatchQuestion" -> DragQuestion(context!!, metaQuestion.questionBase as MatchQuestionBase, image)
                    //"OneWordQuestion" -> OneWordQuestion(context!!, metaQuestion.questionBase as OneWordQuestionBase, image)
                    //"ChronologicalQuestion" -> ChronologicalQuestion(context!!, metaQuestion.questionBase as ChronologicalQuestionBase, image)

                    "OneWordQuestionEGE"        -> OneWordQuestionEGE(context!!, metaQuestion.questionBase as OneWordQuestionEGE_Base, image)
                    "SequenceQuestionEGE"       -> SequenceQuestionEGE(context!!, metaQuestion.questionBase as SequenceQuestionEGE_Base, image)
                    "MultipleAnswerQuestionEGE" -> MultipleAnswerQuestionEGE(context!!, metaQuestion.questionBase as MultipleAnswerQuestionEGE_Base, image)
                    "MatchQuestionEGE"          -> MatchQuestionEGE(context!!, metaQuestion.questionBase as MatchQuestionEGE_Base, image)
                    "PairMatchQuestionEGE"      -> PairMatchQuestionEGE(context!!, metaQuestion.questionBase as PairMatchQuestionEGE_Base, image)

                    else -> throw Exception("Unknown question type")
                }
            )
        }

        questionList = questions
    }

    private fun setTimer(){
        timer.visibility = VISIBLE
        startTime = System.currentTimeMillis()
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    //Ответы
    private fun putTestStat(stat: TestObject){

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val testAPI = retrofit.create(TestStatAPI::class.java)
        val (_,subject, topic, lesson) = path.split("/")
        val token = activity!!.intent.getStringExtra("token")!!
        val jsonStr = Gson().toJson(stat.test)
        testAPI.putTestInfo("Token $token", subject, topic, lesson, jsonStr).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful){
                    if(activity != null) {
                        Toast.makeText(
                            context,
                            "Результат сохранен",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if(activity != null) {
                    Toast.makeText(
                        context,
                        "Ошибка: сервер не отвечает",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    fun showExitAccept(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выход из контроля!")
            .setMessage("""Вы точно хотите выйти из контроля?
                |Текущий результат будет сохранен
                |По окончанию времени тест закроется""".trimMargin())
            .setPositiveButton("Да") { dialog, _ ->
                dialog.cancel()
                val resultList = (viewPager.adapter as TestFragmentPagerAdapter).getGrades()
                putTestStat(TestObject(path, resultList))
                isFinal = false
                activity!!.onBackPressed()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
        val positiveButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL)
        val parent = positiveButton.parent as LinearLayout
        parent.gravity = Gravity.CENTER_HORIZONTAL
        parent.setPadding(0, 0, 0, 20)
        val leftSpacer = parent.getChildAt(1)
        leftSpacer.visibility = GONE
    }

    override fun onStop(){
        if(isFinal) {
            /*val resultList = adapter!!.getGrades()
            */
        }
        super.onStop()
    }

    override fun onDestroyView() {
        timerHandler.removeCallbacks(timerRunnable)
        super.onDestroyView()
    }
}