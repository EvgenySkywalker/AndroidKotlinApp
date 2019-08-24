package com.application.expertnewdesign.statistic

import com.google.gson.annotations.SerializedName

class Subject(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val topicList: List<Topic>
): Statistic()