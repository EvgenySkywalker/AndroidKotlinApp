package com.application.expertnewdesign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.lesson.ArticleFragment
import com.application.expertnewdesign.statistic.MetadataNavigation
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import android.R.attr.path
import kotlinx.android.synthetic.main.loading_fragment.*
import java.io.FileInputStream
import java.nio.file.Files.isDirectory



interface LessonAPI {
    @GET
    fun loadLesson(@Url url: String): Call<ResponseBody>
}

interface MetadataAPI {
    @GET("metadata/")
    fun loadMetadata(): Call<MetadataNavigation>
}

class MetadataLoadingFragment: Fragment(), Callback<MetadataNavigation>{

    private val BASE_URL: String = "http://172.18.10.45:8080/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        getMetadata()
    }

    override fun onResponse(call: Call<MetadataNavigation>, response: Response<MetadataNavigation>) {
        if(response.isSuccessful) {
            fragmentManager!!.beginTransaction().run {
                add(R.id.fragment_container, NavigationLessonsFragment(response.body()), "navigation")
                remove(fragmentManager!!.findFragmentByTag("metadata_loading")!!)
                commit()
            }
        } else {
            println(response.errorBody())
        }
    }

    override fun onFailure(call: Call<MetadataNavigation>, t: Throwable) {
        t.printStackTrace()
    }

    fun getMetadata() {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val metadataAPI = retrofit.create(MetadataAPI::class.java)

        val call = metadataAPI.loadMetadata()
        call.enqueue(this)
    }
}

class LessonLoadingFragment(val lessonPath: String): Fragment(), Callback<ResponseBody> {

    private val BASE_URL: String = "http://172.18.10.45:8080/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        getLesson()
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        if(response.isSuccessful){
            loading_stat.text = "Распаковка данных..."
            /*val zipFile = ZipInputStream(response.body().byteStream())

            val SDPath = context!!.getExternalFilesDir(null)
            val path = "$SDPath$lessonPath/"
            val dir = File(path)
            dir.mkdirs()
            var entry = zipFile.nextEntry
            zipFile.closeEntry()
            entry = zipFile.nextEntry
            while (entry != null){
                val unzipPath = path + entry.name.substring(entry.name.indexOf("\\")+1)
                if (entry.isDirectory) {
                    val unzipFile = File(unzipPath)
                    if (!unzipFile.isDirectory) {
                        unzipFile.mkdirs()
                    }
                }else {
                    val fout = FileOutputStream(unzipPath)
                    try {
                        var c = zipFile.read()
                        while (c != -1) {
                            fout.write(c)
                            c = zipFile.read()
                        }
                        zipFile.closeEntry()
                    } finally {
                        fout.close()
                    }
                }
                entry = zipFile.nextEntry
            }*/
            fragmentManager!!.beginTransaction().run {
                add(R.id.fragment_container, ArticleFragment("$lessonPath/"), "article")
                hide(fragmentManager!!.findFragmentByTag("navigation")!!)
                addToBackStack("lesson_stack")
                commit()
            }
            fragmentManager!!.beginTransaction().run{
                remove(fragmentManager!!.findFragmentByTag("lesson_loading")!!)
                commit()
            }
            activity!!.nav_view.visibility = View.GONE
        }else{
            println(response.errorBody().string())
        }
    }

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        t.printStackTrace()
    }

    fun getLesson() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val lessonAPI = retrofit.create(LessonAPI::class.java)

        val call = lessonAPI.loadLesson(StringBuilder("getLesson?name=").append(lessonPath).toString())
        call.enqueue(this)
    }
}