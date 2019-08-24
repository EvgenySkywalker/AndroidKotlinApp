package com.application.expertnewdesign.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.R
import com.application.expertnewdesign.question.test
import kotlinx.android.synthetic.main.test_fragment.*

class TestFragment(val path: String) : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        setTest()
    }

    private fun setTest() {
        /*val file = File(this.filesDir, "QuestionsPack.xml")
        file.delete()
        file.createNewFile()
        file.bufferedWriter().write(BufferedReader(InputStreamReader(assets.open("QuestionsPack.xml"))).readText())
        val str = file.bufferedReader().readLine()*/
        scrollView.addView(test(activity!!))
        testBack.setOnClickListener {
            activity!!.onBackPressed()
        }
    }
}