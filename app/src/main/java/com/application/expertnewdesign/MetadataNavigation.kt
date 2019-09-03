package com.application.expertnewdesign

import com.google.gson.annotations.SerializedName

class MetadataNavigation(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val subjectList: List<Subject>)

class Subject(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val topicList: List<Topic>
): Statistic()

class Topic(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val lessonList: List<Lesson>
): Statistic()

class Lesson(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
): Statistic()

open class Statistic{

}