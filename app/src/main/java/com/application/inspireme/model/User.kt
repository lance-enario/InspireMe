package com.application.inspireme.model

data class User(
    val email: String = "",
    val username: String = "",
    val bio: String = "No bio available",
    val createdAt: Long = 0
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", 0)
}