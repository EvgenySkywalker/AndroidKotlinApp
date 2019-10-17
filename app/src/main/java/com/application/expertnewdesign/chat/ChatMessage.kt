package com.application.expertnewdesign.chat

import java.util.*


class ChatMessage(var messageText: String? = null, var messageUser: String? = null) {

    var messageTime: Long = 0
    var key: String? = null

    init {
        messageTime = Calendar.getInstance().timeInMillis
    }
}