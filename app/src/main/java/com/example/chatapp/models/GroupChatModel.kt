package com.example.chatapp.models

class GroupChatModel {

    var message: String? = null
    var sender: String? = null
    var timeStamp: String? = null
    var type: String? = null

    constructor()

    constructor(message: String?, sender: String?, timeStamp: String?, type: String?) {
        this.message = message
        this.sender = sender
        this.timeStamp = timeStamp
        this.type = type
    }
}