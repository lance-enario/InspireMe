package com.application.inspireme.model

data class Quote(
    var id: String = "",
    val quote: String = "",
    val author: String = "",
    val authorId: String = "", // Add this field
    val length: Int = 0,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", 0, emptyList())
}

// Keep this class for external API responses
data class QuoteResponse(
    val id: String,
    val quote: String,
    val author: String,
    val length: Int,
    val tags: List<String>
)