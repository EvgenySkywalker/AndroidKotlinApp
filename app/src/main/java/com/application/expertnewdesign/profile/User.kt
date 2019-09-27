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

class TimeObject(
    @SerializedName("lesson")
    val lesson: String,
    @SerializedName("time")
    var time: Long)

class TestObject(
    @SerializedName("lesson")
    val lesson: String,
    @SerializedName("test")
    val test: List<QuestionObject>)

class QuestionObject(
    @SerializedName("QuestionID")
    val questionID: Int,
    @SerializedName("Status")
    val status: Int)