package com.application.inspireme.model

data class Quote(
    val id: String = "",
    val userId: String = "",
    val quote: String = "",
    val author: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = 0,
    val isDiscovery: Boolean = false  // Add this flag
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", "", emptyList(), 0)
}

// Keep this class for external API responses
data class QuoteResponse(
    val id: String,
    val quote: String,
    val author: String,
    val length: Int,
    val tags: List<String>
)