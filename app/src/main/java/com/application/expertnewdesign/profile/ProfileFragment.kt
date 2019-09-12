package com.application.expertnewdesign.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.JsonHelper
import com.application.expertnewdesign.MainActivity
import com.application.expertnewdesign.R
import com.application.expertnewdesign.UserDataSending
import com.application.expertnewdesign.authorization.ui.login.LoginActivity
import com.application.expertnewdesign.navigation.Lesson
import com.application.expertnewdesign.navigation.Statistic
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.profile_fragment.*
import java.io.File

class ProfileFragment : Fragment(){

    var synchronized = false
    lateinit var token: String

    val user : User
        get() = getUserData()

    private var lessonStat: MutableList<TimeObject> = arrayListOf()
    private var testStat: MutableList<Statistic> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    //////
    //
    ////
    //
    //

    override fun onStart() {
        super.onStart()

        token = activity!!.intent.getStringExtra("token")!!

        button.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }

        val activity = activity as MainActivity
        activity.profileFragment = this
        fragmentManager!!.beginTransaction().run{
            hide(activity.profileFragment!!)
            commit()
        }

        setUserData(true)
    }

    private fun getUserData(): User{
        val user = User()
        val list: MutableList<TimeObject> = arrayListOf()
        lessonStat.forEach {
            var has = false
            if(list.isNotEmpty()) {
                for (element in list) {
                    if (it.name == element.name) {
                        element.time += it.time
                        has = true
                        break
                    }
                }
            }
            if(!has){
                list.add(it)
            }
        }
        user.lessonsStat = list
        return user
    }

    fun addStat(stat: Statistic) {
        when(stat){
            is Lesson->{
                lessonStat.add(TimeObject(stat.name, stat.time))
            }
        }
        setUserData(false)
    }

    private fun setUserData(fromFile: Boolean){
        fun getStr(lessonsStat: List<TimeObject>): String{
            var lessonTime: Long = 0
            lessonsStat.forEach {
                lessonTime += it.time
            }
            lessonTime /= 1000
            val min = lessonTime / 60
            val sec = lessonTime % 60
            return "$min:$sec"
        }

        if(fromFile) {
            val file = File("${activity!!.filesDir.path}/user.json")
            if (file.exists()) {
                val json = JsonHelper("${activity!!.filesDir.path}/user.json")
                val user = json.user
                time.text = getStr(user.lessonsStat)
            }
            lessonStat.addAll(user.lessonsStat)
        }else{
            time.text = getStr(lessonStat)
        }
    }
}