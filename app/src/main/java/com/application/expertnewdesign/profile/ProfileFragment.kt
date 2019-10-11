package com.application.expertnewdesign.profile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
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

interface UserStatAPI{
    @GET("getSelfUserStats")
    fun getUserStat(@Header("Authorization") token: String): Call<List<TimeObject>>
}

class ProfileFragment : Fragment(){

    private lateinit var token: String

    val user : User
        get() = getUserData()

    private var neTotName: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var rights: String? = null

    val profileDataHandler = Handler()
    private val dataRunnable = Runnable {
        getUserInfo()
    }

    private var lessonStat: MutableList<TimeObject> = emptyList<TimeObject>().toMutableList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = activity!!.intent.getStringExtra("token")!!
        val activity = activity as MainActivity
        activity.profileFragment = this

        loadUser()
        setLogout()
    }

    private fun setLogout(){
        button.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.putExtra("relogin", true)
            Thread().run{
                val file = File("${activity!!.filesDir.path}/token.txt")
                file.delete()
            }
            activity!!.startActivity(intent)
            activity!!.finish()
        }
    }

    private fun getUserData(): User{
        val user = User()
        if(neTotName != null) {
            user.name = neTotName
            user.firstName = firstName
            user.lastName = lastName
            user.rights = rights
        }
        val lessonList: MutableList<TimeObject> = emptyList<TimeObject>().toMutableList()
        if(lessonStat.isNotEmpty()) {
            lessonStat.forEach {
                var has = false
                if (lessonList.isNotEmpty()) {
                    for (element in lessonList) {
                        if (it.lesson == element.lesson) {
                            element.time += it.time
                            has = true
                            break
                        }
                    }
                }
                if (!has) {
                    lessonList.add(it)
                }
            }
        }
        user.lessonsStat = lessonList
        return user
    }

    fun addStat(stat: Statistic) {
        when(stat){
            is Lesson->{
                lessonStat.add(TimeObject(stat.name, stat.time))
            }
        }
        setUserData()
    }

    private fun getUserInfo(){
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val userAPI = retrofit.create(UserInfoAPI::class.java)
        userAPI.getUserInfo("Token $token").enqueue(object: Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful){
                    Thread().run{
                        infinite_loading.visibility = GONE
                        loading_stat.visibility = GONE
                        val user = response.body()
                        if(user.name != null) {
                            neTotName = user.name
                            firstName = user.firstName
                            lastName = user.lastName
                            rights = getRightsRU(user.rights!!)
                            val chatFragment = activity!!.supportFragmentManager
                                .findFragmentByTag("chat") as ChatFragment
                            when (name) {
                                "admin" -> {
                                    chatFragment.username = "$firstName"
                                }
                                else -> {
                                    chatFragment.username = "$firstName($name)"
                                }
                            }
                            activity!!.runOnUiThread {
                                setUserData()
                            }
                        }
                    }
                    getUserStat()
                }else{
                    infinite_loading.visibility = GONE
                    loading_stat.text = "Не удалось загрузить данные"
                    profileDataHandler.postDelayed(dataRunnable, 5000)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                infinite_loading.visibility = GONE
                loading_stat.text = "Не удалось загрузить данные"
                profileDataHandler.postDelayed(dataRunnable, 5000)
            }
        })
    }

    private fun getUserStat(){
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val userAPI = retrofit.create(UserStatAPI::class.java)
        userAPI.getUserStat("Token $token").enqueue(object: Callback<List<TimeObject>>{
            override fun onResponse(call: Call<List<TimeObject>>, response: Response<List<TimeObject>>) {
                if(response.isSuccessful){
                    Thread().run {
                        val timeStat = response.body()
                        if(timeStat.isNotEmpty()) {
                            lessonStat.addAll(timeStat)
                            activity!!.runOnUiThread {
                                setUserData()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<TimeObject>>?, t: Throwable?) {

            }
        })
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
            nameView.text = neTotName
            nameView.visibility = VISIBLE
            firstNameView.text = firstName
            profileFirstName.visibility = VISIBLE
            lastNameView.text = lastName
            profileLastName.visibility = VISIBLE
            rightsView.text = rights
            profileRights.visibility = VISIBLE
            time.text = getStr(lessonStat)
            profileLessons.visibility = VISIBLE
    }


    private fun loadUser(){
        getLocal()
        getUserInfo()
    }

    fun getLocal(){
        val userLocal = File("${activity!!.filesDir.path}/user.json")
        if(userLocal.exists()) {
            val json = JsonHelper("${activity!!.filesDir.path}/user.json")
            val user = json.user
            if(user.name != null) {
                neTotName = user.name!!
                firstName = user.firstName!!
                lastName = user.lastName
                rights = getRightsRU(user.rights!!)
                lessonStat.addAll(user.lessonsStat)
                activity!!.runOnUiThread {
                    setUserData()
                }
            }
            userLocal.delete()
        }
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
                return value
            }
        }
    }

    override fun onDestroy() {
        profileDataHandler.removeCallbacks(dataRunnable)
        super.onDestroy()
    }
}