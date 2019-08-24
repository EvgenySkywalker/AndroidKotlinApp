package com.application.expertnewdesign

import com.google.gson.annotations.SerializedName

class Video(
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String)