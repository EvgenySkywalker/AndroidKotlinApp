package com.application.expertnewdesign.statistic

import com.google.gson.annotations.SerializedName

class Topic(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val lessonList: List<Lesson>
): Statistic()