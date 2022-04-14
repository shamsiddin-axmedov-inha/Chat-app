package com.example.chatapp.models

class GroupChatModelList {
    var groupId: String? = null
    var groupTitle: String? = null
    var groupDescription: String? = null
    var timeStamp: String? = null
    var createdBy: String? = null

    constructor()

    constructor(
        groupId: String?,
        groupTitle: String?,
        groupDescription: String?,
        timeStamp: String?,
        createdBy: String?
    ) {
        this.groupId = groupId
        this.groupTitle = groupTitle
        this.groupDescription = groupDescription
        this.timeStamp = timeStamp
        this.createdBy = createdBy
    }
}