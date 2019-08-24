package com.application.expertnewdesign.statistic

import com.google.gson.annotations.SerializedName

class Lesson(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
): Statistic()