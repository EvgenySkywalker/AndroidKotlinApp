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
import android.view.View.GONE
import kotlinx.android.synthetic.main.loading_fragment.*
import retrofit2.http.Query
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files.isDirectory
import java.util.zip.ZipEntry

interface LessonAPI {
    @GET
    fun loadLesson(@Url url: String): Call<ResponseBody>
}

interface MetadataAPI {
    @GET("metadata/")
    fun loadMetadata(): Call<MetadataNavigation>
}

val BASE_URL: String = "http://35.207.89.62:8080/"

class MetadataLoadingFragment: Fragment(), Callback<MetadataNavigation>{

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
        progressBarLoading.visibility = GONE
        loading_stat.text = "RIP backend"
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        getLesson()
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        if(response.isSuccessful){

            val SDPath = context!!.getExternalFilesDir(null)
            val path = "$SDPath$lessonPath/"

            val fileDir = File(path)
            fileDir.mkdirs()

            loadFile(response.body().byteStream(), path)

            val zipFile = File(path+"lesson.zip")
            zipFile.unzipLesson(fileDir)
            zipFile.delete()

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
            activity!!.nav_view.visibility = GONE
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

    fun loadFile(byteStream: InputStream, path: String){
        val zipPath = path+"lesson.zip"
        val fout = FileOutputStream(zipPath)
        try {
            var c = byteStream.read()
            while (c != -1) {
                fout.write(c)
                c = byteStream.read()
            }
        } finally {
            fout.close()
        }
    }

    fun File.unzipLesson(dest : File){
        fun ZipEntry.is_directory() : Boolean{
            return name.endsWith("/")
        }

        ZipFile(this).use { zipFile ->
            zipFile.entries().asSequence().forEach { entry ->
                if(!entry.is_directory()){
                    zipFile.getInputStream(entry).use{ entryStream ->
                        File(dest, entry.name.split("/").last()).outputStream().use { fileStream ->
                            entryStream.copyTo(fileStream)
                        }
                    }
                }
            }
        }
    }
}