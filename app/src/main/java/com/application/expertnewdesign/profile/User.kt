package com.application.expertnewdesign.profile
import com.google.gson.annotations.SerializedName

class User{
    @SerializedName("username")
    var name: String? = null
    @SerializedName("firstName")
    var firstName: String? = null
    @SerializedName("lastName")
    var lastName: String? = null
    @SerializedName("rights")
    var rights: String? = null
    @SerializedName("lessons")
    var lessonsStat: List<TimeObject> = emptyList()
    @SerializedName("tests")
    var testsStat: List<TestObject> = emptyList()
}

data class TimeObject(
    @SerializedName("lesson")
    val lesson: String,
    @SerializedName("time")
    var time: Long)

data class TestObject(
    @SerializedName("lesson")
    val lesson: String,
    @SerializedName("test")
    val test: List<QuestionObject>)

data class QuestionObject(
    @SerializedName("questionID")
    val questionID: Int,
    @SerializedName("status")
    val status: Int)