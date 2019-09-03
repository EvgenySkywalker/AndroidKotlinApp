package com.application.expertnewdesign

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

class JsonHelper(private val path: String){

    private val reader: BufferedReader
        get() = File(path).reader(Charsets.UTF_8).buffered()

    val listVideo: List<String>
        get() = getVideoList()

    val listSubject: List<Subject>
        get() = getSubjectList()

    private fun getVideoList(): List<String>{
        val collectionType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(reader, collectionType)
    }

    private fun getSubjectList(): List<Subject>{
        val collectionType = object : TypeToken<List<Subject>>() {}.type
        return Gson().fromJson(reader.readText(), collectionType)
    }
}