package com.application.inspireme.model

data class UserNotification(
    val id: String = "",
    val senderId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // NEW_FOLLOWER, NEW_QUOTE, LIKE_QUOTE
    val relatedId: String? = null, // quoteId, userId, etc.
    val timestamp: Long = 0,
    val read: Boolean = false
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", "", null, 0, false)
}