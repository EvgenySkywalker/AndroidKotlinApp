package com.application.expertnewdesign.navigation
import com.google.gson.annotations.SerializedName

data class MetadataNavigation(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val subjectList: List<Subject>)

data class Subject(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val topicList: List<Topic>
): Statistic()

data class Topic(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val lessonList: List<Lesson>
): Statistic()

data class Lesson(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("lastUpdate")
    val lastUpdate: Long? = -1
): Statistic()

open class Statistic{
    var time: Long = 0
}