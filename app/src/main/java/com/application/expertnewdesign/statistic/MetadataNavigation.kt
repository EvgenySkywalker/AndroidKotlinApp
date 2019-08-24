package com.application.expertnewdesign.statistic

import com.google.gson.annotations.SerializedName

class MetadataNavigation(
    @SerializedName("name")
    val name: String,
    @SerializedName("contents")
    val subjectList: List<Subject>)