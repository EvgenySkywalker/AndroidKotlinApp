package com.application.expertnewdesign.profile

import com.application.expertnewdesign.navigation.Statistic
import com.google.gson.annotations.SerializedName

class User{
    @SerializedName("lessons")
    var lessonsStat: List<TimeObject> = emptyList()
    @SerializedName("tests")
    var testsStat: List<Statistic> = emptyList()
}

class TimeObject(
    @SerializedName("name")
    val name: String,
    @SerializedName("time")
    var time: Long)