package com.application.expertnewdesign

import android.app.IntentService
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.lesson.article.ArticleFragment
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.StringBuilder
import java.util.zip.ZipFile
import android.view.View.GONE
import kotlinx.android.synthetic.main.loading_fragment.*
import java.util.zip.ZipEntry
import android.os.Parcel
import android.os.Parcelable
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.application.expertnewdesign.authorization.ui.login.LoginActivity
import com.application.expertnewdesign.navigation.MetadataNavigation
import com.application.expertnewdesign.navigation.NavigationLessonsFragment
import com.application.expertnewdesign.profile.User
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.http.*
import java.io.*
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.ProtocolException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.roundToInt


interface LessonAPI {
    @GET("getLesson")
    @Streaming
    fun loadLesson(@Header("Authorization") token: String,
                   @Query("course") subject: String,
                   @Query("subject") topic: String,
                   @Query("name") lesson: String): Call<ResponseBody>
}

//fetch('http://172.18.10.45:8080/getLesson?course=123&subject=123&name=a', {method:'GET', headers: {'Authorisation':'Token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwicmlnaHRzIjoiYWRtaW4ifQ.f1Tn8KG7TlnDegM6UWYa5cTL6Iz4TaI0xNsLqMnA0Wk'}})
interface MetadataAPI {
    @GET("metadata/")
    fun loadMetadata(@Header("Authorization") token: String): Call<MetadataNavigation>
}

val BASE_URL: String = "http://35.228.251.136:8080/"//172.18.10.45   35.228.251.136

class MetadataLoadingFragment: Fragment(), Callback<MetadataNavigation>{

    private lateinit var token: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        horizontalProgress.visibility = GONE
        infinite_loading.visibility = VISIBLE
        token = activity!!.intent.getStringExtra("token")!!
        getMetadata()
    }

    override fun onResponse(call: Call<MetadataNavigation>, response: Response<MetadataNavigation>) {
        if(response.isSuccessful) {
            val metadata = response.body()
            fragmentManager!!.beginTransaction().run {
                add(R.id.fragment_container, NavigationLessonsFragment(metadata), "navigation")
                remove(fragmentManager!!.findFragmentByTag("metadata_loading")!!)
                commit()
            }
            Thread().run{
                val json = JsonHelper(activity!!.getExternalFilesDir(null).toString())
                json.toJson(metadata)
            }
        } else {
            if(response.raw().message() == "Unauthorized"){
                Toast.makeText(context, "Пользователь более не действителен", Toast.LENGTH_SHORT).show()
                Thread().run{
                    val file = File("${activity!!.filesDir.path}/token.txt")
                    file.delete()
                    val file2 = File("${activity!!.filesDir.path}/user.data")
                    file2.delete()
                }
                val intent = Intent(activity, LoginActivity::class.java)
                activity!!.startActivity(intent)
                activity!!.finish()
            }
            infinite_loading.visibility = GONE
            loading_stat.text = "Ошибка входа"
        }
    }

    override fun onFailure(call: Call<MetadataNavigation>, t: Throwable) {
        val file = File("${activity!!.getExternalFilesDir(null).toString()}/metadata.json")
        if(file.exists()) {
            val json = JsonHelper("${activity!!.getExternalFilesDir(null).toString()}/metadata.json")
            fragmentManager!!.beginTransaction().run {
                add(R.id.fragment_container, NavigationLessonsFragment(json.metadata), "navigation")
                remove(fragmentManager!!.findFragmentByTag("metadata_loading")!!)
                commit()
            }
        }else{
            infinite_loading.visibility = GONE
            loading_stat.text = "Сервер не отвечает"
        }
        Toast.makeText(context, "Не удалось подключиться к серверу", Toast.LENGTH_SHORT).show()
    }

    private fun getMetadata() {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val metadataAPI = retrofit.create(MetadataAPI::class.java)

        val call = metadataAPI.loadMetadata("Token $token")
        call.enqueue(this)
    }
}

class LessonLoadingFragment(val lessonPath: String, time: Long = -1): Fragment(){

    lateinit var intent: Intent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        getLesson()
    }

    private fun getLesson() {
            val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
            val intentFilter = IntentFilter()
            intentFilter.addAction(lessonPath)

            intent = Intent(context, DownloadService::class.java)
            intent.putExtra("path", lessonPath)
            intent.putExtra("token", activity!!.intent.getStringExtra("token"))

            bManager.registerReceiver(broadcastReceiver, intentFilter)
            activity!!.startService(intent)
    }

    fun File.unzipLesson(dest : File){
        fun ZipEntry.is_directory() : Boolean{
            return name.endsWith("/")
        }

        ZipFile(this).use { zipFile ->
            zipFile.entries().asSequence().forEach { entry ->
                if(!entry.is_directory()){
                    zipFile.getInputStream(entry).use{ entryStream ->
                        File(dest, entry.name.substringAfterLast("/")).outputStream().use { fileStream ->
                            entryStream.copyTo(fileStream)
                        }
                    }
                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, _intent: Intent) {

            if (_intent.action == lessonPath) {

                val download = _intent.getParcelableExtra<Download>("download")
                if(horizontalProgress != null) {
                    if(horizontalProgress.progress > download!!.progress){
                        return
                    }
                    horizontalProgress.progress = download.progress
                }
                if (download!!.progress == 100) {
                    val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
                    bManager.unregisterReceiver(this)
                    if(loading_stat != null) {
                        loading_stat.text = "Загрузка урока завершена"
                        loading_stat.text = "Распаковка..."
                    }
                    Thread().run {
                        val path = "${context.getExternalFilesDir(null).toString()}$lessonPath"
                        val dir = File(path)

                        val zipFile = File(path, "lesson.zip")
                        zipFile.unzipLesson(dir)
                        zipFile.delete()

                        fragmentManager!!.beginTransaction().run {
                            add(R.id.fragment_container, ArticleFragment("$lessonPath/", time), "article")
                            hide(fragmentManager!!.findFragmentByTag("navigation")!!)
                            addToBackStack("lesson_stack")
                            commit()
                        }
                        activity!!.stopService(intent)
                        activity!!.supportFragmentManager.beginTransaction().run{
                            remove(fragmentManager!!.findFragmentByTag("lesson_loading")!!)
                            commit()
                        }
                        activity!!.runOnUiThread {
                            activity!!.nav_view.visibility = GONE
                        }
                    }
                } else {
                    if(loading_stat != null)
                        loading_stat.text =
                            String.format(
                                "Загружено %d,%d из %d,%d МБ",
                                download.currentFileSize/1024,
                                (download.currentFileSize/102)%10,
                                download.totalFileSize/1024,
                                (download.totalFileSize/102)%10
                            )
                }
            }else{
                val download = _intent.getParcelableExtra<Download>("download")
                if (download!!.progress == 100) {
                    Thread().run {
                        val path = "${context.getExternalFilesDir(null).toString()}${_intent.action}"
                        val dir = File(path)

                        val zipFile = File(path, "lesson.zip")
                        zipFile.unzipLesson(dir)
                        zipFile.delete()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
        bManager.unregisterReceiver(broadcastReceiver)
        activity!!.stopService(intent)
    }
}

class DownloadService : IntentService("Download Service") {

    private var downloadError = false
    private lateinit var lessonPath: String
    private lateinit var token: String
    private var totalFileSize: Int = 0
    private var intent: Intent? = null

    override fun onHandleIntent(intent: Intent?) {

        this.intent = intent

        lessonPath = intent!!.getStringExtra("path")!!
        token = intent.getStringExtra("token")!!

        initDownload()
    }

    private fun initDownload(){

        val innerClient = OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES) // read timeout
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(innerClient)
            .build()

        val lessonAPI = retrofit.create(LessonAPI::class.java)

        val (subject, topic, lesson) = lessonPath.substringAfter("/").split("/")
        val request = lessonAPI.loadLesson("Token $token", subject, topic, lesson)

            try {
                downloadFile(request.execute().body())
            } catch (e: TimeoutException) {
                downloadError = true
                if(intent != null){
                    stopService(intent)
                    intent = null
                }
            } catch (i: IllegalStateException){
                downloadError = true
                if(intent != null){
                    stopService(intent)
                    intent = null
                }
            } catch (c: ConnectException){
                downloadError = true
                if(intent != null){
                    stopService(intent)
                    intent = null
                }
            }
    }


    private fun downloadFile(body: ResponseBody){

        var count: Int
        val data = ByteArray(1024 * 4)
        val fileSize: Long = body.contentLength()
        val bis = BufferedInputStream(body.byteStream(), 1024 * 8)

        val dir = File("${getExternalFilesDir(null).toString()}$lessonPath")
        dir.mkdirs()

        val outputFile = File("${getExternalFilesDir(null).toString()}${lessonPath}/lesson.zip")
        val output = FileOutputStream(outputFile)
        var total: Long = 0
        val startTime: Long = System.currentTimeMillis()
        var timeCount = 1
        count = bis.read(data)
        while (count != -1) {
            total += count

            totalFileSize = (fileSize / 1024.0).toInt()

            val current = (total / 1024.0).roundToInt()

            val progress = ((total * 100) / fileSize).toInt()

            val currentTime = System.currentTimeMillis() - startTime

            val download = Download()

            download.totalFileSize = totalFileSize

            if (currentTime > 1000 * timeCount) {

                download.currentFileSize = current
                download.progress = progress
                sendNotification(download)
                timeCount++
            }

            output.write(data, 0, count)
            try {
                count = bis.read(data)
            }catch (p: ProtocolException){
                downloadError = true
            }
        }
        onDownloadComplete()
        output.flush()
        output.close()
        bis.close()
    }

    private fun sendNotification(download: Download){
        sendIntent(download)
    }

    private fun sendIntent(download: Download){

        val intent = Intent(lessonPath)
        intent.putExtra("download",download)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun onDownloadComplete(){
        if(!downloadError) {
            val download = Download()
            download.progress = 100
            sendIntent(download)
        }else{
            val download = Download()
            download.progress = 0
            sendIntent(download)
        }
    }
}

class Download() : Parcelable {

    var progress: Int = 0
    var currentFileSize: Int = 0
    var totalFileSize: Int = 0

    private constructor (`in`: Parcel): this(){
        progress = `in`.readInt()
        currentFileSize = `in`.readInt()
        totalFileSize = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {

        dest.writeInt(progress)
        dest.writeInt(currentFileSize)
        dest.writeInt(totalFileSize)
    }

    companion object CREATOR : Parcelable.Creator<Download> {
        override fun createFromParcel(parcel: Parcel): Download {
            return Download(parcel)
        }

        override fun newArray(size: Int): Array<Download?> {
            return arrayOfNulls(size)
        }
    }
}