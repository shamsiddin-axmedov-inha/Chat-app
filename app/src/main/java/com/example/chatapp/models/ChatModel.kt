package com.example.chatapp.models

class ChatModel {
    var message: String? = null
    var receiver: String? = null
    var sender: String? = null
    var time: String? = null
    var isSeen: String? = null

    constructor()

    constructor(
        message: String?,
        receiver: String?,
        sender: String?,
        time: String?,
        isSeen: String?
    ) {
        this.message = message
        this.receiver = receiver
        this.sender = sender
        this.time = time
        this.isSeen = isSeen
    }
}