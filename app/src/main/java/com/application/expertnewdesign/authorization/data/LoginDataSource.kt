package com.application.expertnewdesign.authorization.data

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.application.expertnewdesign.BASE_URL
import com.application.expertnewdesign.MainActivity
import com.application.expertnewdesign.authorization.data.model.LoggedInUser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.File
import java.io.IOException
import java.security.MessageDigest

interface loginAPI{
    @POST("login")
    fun authentification(@Query("username") username: String, @Query("passwordHash") password: String): Call<ResponseBody>
}
class LoginDataSource(
    val act: AppCompatActivity
) {

    fun login(username: String, password: String) {
        //try {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()

            val loginAPI = retrofit.create(loginAPI::class.java)

            val call = loginAPI.authentification(username, hash(password))
            call.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful){
                        val intent = Intent(act, MainActivity::class.java).apply {
                            putExtra("token", response.body().string())
                        }
                        act.startActivity(intent)
                        act.finish()
                    }else{
                        println("fuck")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    println("fuck")
                }
            })
        /*} catch (e: Throwable) {

        }*/
    }

    fun logout() {
        // TODO: revoke authentication
    }

    fun hash(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}

