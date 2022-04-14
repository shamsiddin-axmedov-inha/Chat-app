package com.example.chatapp.models

import java.io.Serializable

class User: Serializable {
    var email: String? = null
    var username: String? = null
    var uid: String? = null

    constructor()

    constructor(
        email: String?,
        username: String?,
        uid: String?
    ) {
        this.email = email
        this.username = username
        this.uid = uid
    }


}