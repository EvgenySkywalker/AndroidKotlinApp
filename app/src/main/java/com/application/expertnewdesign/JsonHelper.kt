package com.application.expertnewdesign

import com.application.expertnewdesign.navigation.MetadataNavigation
import com.application.expertnewdesign.navigation.Subject
import com.application.expertnewdesign.profile.QuestionObject
import com.application.expertnewdesign.profile.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

class JsonHelper(private val path: String){

    private val reader: BufferedReader?
        get() = getJsonReader()

    val listVideo: List<String>?
        get() = getVideoList()

    val metadata: MetadataNavigation
        get() = mFromJson()

    val user: User
        get() = uFromJson()

    private fun getJsonReader(): BufferedReader?{
        val file = File(path)
        if(file.exists()){
            return file.reader(Charsets.UTF_8).buffered()
        }
        return null
    }

    private fun getVideoList(): List<String>?{
        val collectionType = object : TypeToken<List<String>>() {}.type
        if(reader != null) {
            return Gson().fromJson(reader, collectionType)
        }
        return null
    }

    fun toJson(metadata: MetadataNavigation){
        val jsonStr = Gson().toJson(metadata)
        val file = File("$path/metadata.json")
        file.bufferedWriter().use{
            it.write(jsonStr)
        }
    }

    private fun mFromJson(): MetadataNavigation{
        val collectionType = object: TypeToken<MetadataNavigation>() {}.type
        return Gson().fromJson(reader, collectionType)
    }

    fun toJson(user: User){
        val jsonStr = Gson().toJson(user)
        val file = File("$path/user.json")
        file.bufferedWriter().use{
            it.write(jsonStr)
        }
    }

    private fun uFromJson(): User{
        val collectionType = object: TypeToken<User>() {}.type
        return Gson().fromJson(reader, collectionType)
    }
}