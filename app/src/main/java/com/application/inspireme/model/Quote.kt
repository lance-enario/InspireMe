package com.application.inspireme.model

data class QuoteResponse(
    val id: String,
    val quote: String,
    val author: String,
    val length: Int,
    val tags: List<String>
)