package com.application.expertnewdesign

import android.content.res.AssetManager
import com.application.expertnewdesign.statistic.Subject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

class JsonHelper(private val path: String){

    private val reader: BufferedReader
        get() = File(path).reader(Charsets.UTF_8).buffered()

    val listVideo: List<Video>
        get() = getVideoList()

    val listSubject: List<Subject>
        get() = getSubjectList()

    private fun getVideoList(): List<Video>{
        val collectionType = object : TypeToken<List<Video>>() {}.type
        return Gson().fromJson(reader, collectionType)
    }

    private fun getSubjectList(): List<Subject>{
        val collectionType = object : TypeToken<List<Subject>>() {}.type
        return Gson().fromJson(reader.readText(), collectionType)
    }
}