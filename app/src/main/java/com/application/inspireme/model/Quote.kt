package com.application.inspireme.model

data class Quote(
    val id: String = "",
    val quote: String = "",
    val author: String = "",
    val length: Int = 0,
    val tags: List<String> = emptyList()
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", 0, emptyList())
}

// Keep this class for external API responses
data class QuoteResponse(
    val id: String,
    val quote: String,
    val author: String,
    val length: Int,
    val tags: List<String>
)