package com.application.expertnewdesign.profile

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.BASE_URL
import com.application.expertnewdesign.JsonHelper
import com.application.expertnewdesign.MainActivity
import com.application.expertnewdesign.R
import com.application.expertnewdesign.authorization.ui.login.LoginActivity
import com.application.expertnewdesign.chat.ChatFragment
import com.application.expertnewdesign.navigation.Lesson
import com.application.expertnewdesign.navigation.Statistic
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.profile_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.io.File

interface UserInfoAPI{
    @GET("getSelfInfo")
    fun getUserInfo(@Header("Authorization") token: String): Call<User>
}

class ProfileFragment : Fragment(){

    var synchronized = false
    lateinit var token: String

    val user : User
        get() = getUserData()

    private var name: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var rights: String? = null

    private var lessonStat: MutableList<TimeObject> = arrayListOf()
    private var testStat: MutableList<TestObject> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = activity!!.intent.getStringExtra("token")!!

        val activity = activity as MainActivity
        activity.profileFragment = this

        button.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.putExtra("relogin", true)
            activity.startActivity(intent)
            activity.finish()
        }

        loadUserInfo()
    }

    private fun getUserData(): User{
        val user = User()
        val lessonList: MutableList<TimeObject> = arrayListOf()
        lessonStat.forEach {
            var has = false
            if(lessonList.isNotEmpty()) {
                for (element in lessonList) {
                    if (it.lesson == element.lesson) {
                        element.time += it.time
                        has = true
                        break
                    }
                }
            }
            if(!has){
                lessonList.add(it)
            }
        }
        user.lessonsStat = lessonList
        user.testsStat = testStat
        return user
    }

    fun addStat(stat: Statistic) {
        when(stat){
            is Lesson->{
                lessonStat.add(TimeObject(stat.name, stat.time))
            }
        }
        //setUserData()
    }

    fun addStat(stat: TestObject) {
        testStat.add(stat)
    }

    private fun setUserData(){
        fun getStr(lessonsStat: List<TimeObject>): String{
            var lessonTime: Long = 0
            lessonsStat.forEach {
                lessonTime += it.time
            }
            lessonTime /= 1000
            val min = lessonTime / 60
            return "$min минут(ы)"
        }

            nameView.text = name
            firstNameView.text = firstName
            profileFirstName.visibility = VISIBLE
            lastNameView.text = lastName
            profileLastName.visibility = VISIBLE
            rightsView.text = rights
            profileRights.visibility = VISIBLE
            time.text = getStr(lessonStat)
            profileLessons.visibility = VISIBLE
    }

    fun clearTests(){
        testStat.clear()
    }

    fun clearLessons(){
        lessonStat.clear()
    }

    fun loadUserInfo(){
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val userAPI = retrofit.create(UserInfoAPI::class.java)
        val token = activity!!.intent.getStringExtra("token")!!
        val call = userAPI.getUserInfo("Token $token").enqueue(object: Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful){
                    infinite_loading.visibility = GONE
                    loading_stat.visibility = GONE
                    val user = response.body()
                    name = user.name
                    firstName = user.firstName
                    lastName = user.lastName
                    rights = getRightsRU(user.rights!!)
                    setUserData()
                    val chatFragment = activity!!.supportFragmentManager.findFragmentByTag("chat") as ChatFragment
                    when(name){
                        "admin"->{
                            chatFragment.username = "$firstName"
                        }
                        else->{
                            chatFragment.username = "$firstName($name)"
                        }
                    }
                }else{
                    infinite_loading.visibility = GONE
                    loading_stat.text = "Не удалось загрузить данные"
                    Toast.makeText(
                        context!!, "Пользователь не найден",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                infinite_loading.visibility = GONE
                loading_stat.text = "Не удалось загрузить данные"
                Toast.makeText(
                    context!!, "Сервер не отвечает",
                    Toast.LENGTH_SHORT
                ).show()
            }

            fun getRightsRU(value: String): String{
                when(value){
                    "admin"->{
                        return "Администратор"
                    }
                    "teacher"->{
                        return "Преподаватель"
                    }
                    "student"->{
                        return "Обучающийся"
                    }
                    "guest"->{
                        return "Ученик"
                    }
                    else->{
                        return "Неизвестно"
                    }
                }
            }
        })
    }
}