package com.application.expertnewdesign

import android.app.IntentService
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.application.expertnewdesign.lesson.article.ArticleFragment
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
import com.application.expertnewdesign.navigation.MetadataNavigation
import com.application.expertnewdesign.navigation.NavigationLessonsFragment
import com.application.expertnewdesign.profile.User
import retrofit2.http.Header
import retrofit2.http.Streaming
import java.io.*
import kotlin.math.roundToInt


interface LessonAPI {
    @GET
    @Streaming
    fun loadLesson(@Header("Authorization") token: String, @Url url: String): Call<ResponseBody>
}

interface MetadataAPI {
    @GET("metadata/")
    fun loadMetadata(@Header("Authorization") token: String): Call<MetadataNavigation>
}

val BASE_URL: String = "http://35.228.251.136:8080/"

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
            infinite_loading.visibility = GONE
            loading_stat.text = "Ошибка запроса"
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
            .baseUrl(BASE_URL)//"http://36.207.89.62:8080/"
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val metadataAPI = retrofit.create(MetadataAPI::class.java)

        val call = metadataAPI.loadMetadata("Token $token")
        call.enqueue(this)
    }
}

class LessonLoadingFragment(val lessonPath: String): Fragment(){

    lateinit var intent: Intent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver()
        getLesson()
    }

    private fun getLesson() {
            intent = Intent(context, DownloadService::class.java)
            intent.putExtra("path", lessonPath)
            intent.putExtra("token", activity!!.intent.getStringExtra("token"))
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
                        File(dest, entry.name.split("/").last()).outputStream().use { fileStream ->
                            entryStream.copyTo(fileStream)
                        }
                    }
                }
            }
        }
    }

    private fun registerReceiver() {

        val bManager = LocalBroadcastManager.getInstance(activity!!.applicationContext)
        val intentFilter = IntentFilter()
        intentFilter.addAction("message_progress")
        bManager.registerReceiver(broadcastReceiver, intentFilter)

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, _intent: Intent) {

            if (_intent.action == "message_progress") {

                val download = _intent.getParcelableExtra<Download>("download")
                if(horizontalProgress != null)
                    horizontalProgress.progress = download!!.progress
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
                            add(R.id.fragment_container,
                                ArticleFragment("$lessonPath/"), "article")
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
                                "Загружено (%d/%d) KB",
                                download.currentFileSize,
                                download.totalFileSize
                            )
                }
            }
        }
    }
}

class DownloadService : IntentService("Download Service") {

    val CHANNEL_ID = "loading_lesson"
    private lateinit var lessonPath: String
    private lateinit var token: String
    private lateinit var  notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var totalFileSize: Int = 0

    override fun onHandleIntent(intent: Intent?) {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Загрузка")
            .setContentText("Скачивание урока")
            .setSmallIcon(R.drawable.ic_download)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())

        lessonPath = intent!!.getStringExtra("path")!!
        token = intent.getStringExtra("token")!!

        initDownload()
    }

    fun initDownload(){

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val lessonAPI = retrofit.create(LessonAPI::class.java)

        val request = lessonAPI.loadLesson("Token $token", StringBuilder("getLesson?name=").append(lessonPath).toString())

        try {

            downloadFile(request.execute().body())

        } catch (e: IOException) {

            e.printStackTrace()
            Toast.makeText(applicationContext,e.message, Toast.LENGTH_SHORT).show()

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
            count = bis.read(data)
        }
        onDownloadComplete()
        output.flush()
        output.close()
        bis.close()
    }

    private fun sendNotification(download: Download){

        sendIntent(download)
        notificationBuilder.setProgress(100,download.progress,false)
        notificationBuilder.setContentText("Загрузка урока ${download.currentFileSize}/$totalFileSize КБ")
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendIntent(download: Download){

        val intent = Intent("message_progress")
        intent.putExtra("download",download)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun onDownloadComplete(){

        val download = Download()
        download.progress = 100
        sendIntent(download)

        notificationManager.cancel(0)
        notificationBuilder.setProgress(0,0,false)
        notificationBuilder.setContentText("Урок загружен")
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        notificationManager.cancel(0)
        super.onTaskRemoved(rootIntent)
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